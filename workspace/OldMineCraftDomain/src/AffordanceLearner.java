package burlap.domain.singleagent.minecraft;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.ValueFunctionPlanner;
import burlap.behavior.singleagent.planning.deterministic.TFGoalCondition;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.common.MultiplePFTF;
import burlap.oomdp.singleagent.common.SinglePFTF;

public class AffordanceLearner {
	
	private KnowledgeBase<Affordance> affordanceKB;
	private List<PropositionalFunction> lgds;
	private int numWorldsPerLGD = 5000;
	private MinecraftBehavior mcb;
	private MCStateGenerator mcsg;
	private int numTrajectoriesPerWorld = 1;
	private Random				PRG = new Random();

	public AffordanceLearner(MinecraftBehavior mcb, MCStateGenerator mcsg, KnowledgeBase<Affordance> affordances, List<PropositionalFunction> lgds) {
		double[] numUpdates = {0.0};
		int numRollouts = 1000;
		int maxDepth = 250;
		this.affordanceKB = affordances;
		this.lgds = lgds;
		this.mcb = mcb;
		this.mcsg = mcsg;
		
	}
	
	
	public void learn() {
		
		List<String> maps = new ArrayList<String>();
		
		for(PropositionalFunction goal : this.lgds){
			for (int i = 0; i < this.numWorldsPerLGD; i++) {
				// Make a new map w/ that goal, save it to a file in maps/learning/goal/<name>
				String mapfile = this.mcsg.makeLearningMap(goal,i);
				maps.add(mapfile);
			}
		}
		
		for(String map : maps) {
			learnMap(map);
		}
	}
	
	private State getRandInitialState(OOMDPPlanner p) {
		
		List<State> states = ((ValueFunctionPlanner)p).getAllStates();
		int i = PRG.nextInt(states.size());
		
		State st = states.get(i);
		
		// Being on the floor is equivalent to being in an unsolvable state
		while (agentOnFloor(st)) {
			i = PRG.nextInt(states.size());
			st = states.get(i);
		}
		
		return st;
	}
	
	private boolean agentOnFloor(State st) {
		ObjectInstance agent = st.getObject("agent0");
		int az = agent.getDiscValForAttribute("z");
		return az <= 1;
	}
	
	private void learnMap(String map) {
		// Update behavior with new map
		this.mcb.updateMap(map);
		System.out.println("\n\nLearning with map: " + map);
		
		// Initialize behavior and planner
		OOMDPPlanner planner = new ValueIteration(mcb.domain, mcb.rf, mcb.tf, mcb.gamma, mcb.hashingFactory, 0.01, Integer.MAX_VALUE);

		/**
		 * We iterate through each state in the formed policy and get its "optimal" action. For each affordance,
		 * if that affordance is applicable in the state we increment its action count for the "optimal" action.
		 * 
		 * Note: We DO NOT want to increment an affordance's action count more than once for any given action. To
		 * avoid this we keep track of the actions that have been incremented so far for each affordance in the seen
		 * variable.
		 */
		
		// Form a policy on the given map
		Policy p = mcb.solve(planner);
		Map<Affordance,List<Action>> seen = new HashMap<Affordance,List<Action>>();  // Makes sure we don't count an action more than once per affordance (per map)
		
		// Get a new initial state for each given map
		for (int i = 0; i < numTrajectoriesPerWorld ; i++) {

//			State initialState = getRandInitialState(planner);
			State initialState = mcb.initialState;
			double initVal = ((ValueFunctionPlanner)planner).value(initialState);
			 
//			EpisodeAnalysis ea = p.evaluateBehavior(initialState, mcb.rf, mcb.tf, 100);
//			System.out.println("Initial State:\n" + initialState.getObject("agent0").getObjectDescription());
//			System.out.println("Trajectory: " + ea.getActionSequenceString());
//			for (State st: ea.stateSequence) {
			for (State st: ((ValueFunctionPlanner)planner).getAllStates()) {
				
				double stVal = ((ValueFunctionPlanner)planner).value(st); 
				if ( stVal < 10 * initVal) {  // NOTE: 10 is kind of randomly picked
					continue;
				}
				
				if (mcb.tf.isTerminal(st)) {
					continue;
				}
				GroundedAction ga = p.getAction(st);
				QValue qv = ((ValueFunctionPlanner)planner).getQ(st, ga);
				System.out.println("Action: " + ga.actionName() + " QValue: " + qv.q);
				for (Affordance aff: affordanceKB.getAll()) {
					// Initialize key-value pair for this aff
					if (seen.get(aff) == null) {
						seen.put(aff, new ArrayList<Action>());
					}
					
					// If affordance is lit up
					if (aff.isApplicable(st, ((MultiplePFTF)mcb.tf).getGoalPF())) {
						// If we haven't counted this action for this affordance yet
						if (!seen.get(aff).contains(ga.action)) {
							System.out.println("Learned " + ga.actionName() + " for " + aff.getPreCondition().getName() + "\n" + st.getObject("agent0").getObjectDescription());
							aff.updateActionCount(ga.action);
							List<Action> acts = seen.get(aff);
							acts.add(ga.action);
							seen.put(aff, acts);
						}
					}
				}
			}
		}
		for (Affordance aff: affordanceKB.getAll()) {
			if (seen == null || seen.get(aff) == null) {
				int x  =1 ;
			}
			if (seen.get(aff).size() > 0) {
				aff.updateActionSetSizeCount(seen.get(aff).size());
			}
		}
	}
	
	public static KnowledgeBase<Affordance> generateAffordanceKB(List<PropositionalFunction> predicates, List<PropositionalFunction> lgds, List<Action> allActions) {
		List<Affordance> affordances = new ArrayList<Affordance>();
		
		for (PropositionalFunction pf : predicates) {
			for (PropositionalFunction lgd : lgds) {
				Affordance aff = new Affordance (pf, lgd, allActions);
				affordances.add(aff);
			}
		}
		
		return new KnowledgeBase<Affordance>(Affordance.class, affordances);
		
	}
	
	public void printCounts() {
		for (Affordance aff: this.affordanceKB.getAll()) {
			aff.printCounts();
			System.out.println("");
		}
	}
	
	public static void main(String[] args) {
		MinecraftBehavior mb = new MinecraftBehavior("");
		MCStateGenerator mcsg = new MCStateGenerator();
		
		List<Action> allActions = mb.mcdg.getActions();
		
		// Set up goal description list
		List<PropositionalFunction> lgds = new ArrayList<PropositionalFunction>();
		PropositionalFunction atGoal = mb.pfAgentAtGoal;
		lgds.add(atGoal);
		
		// Set up predicate list
		List<PropositionalFunction> predicates = new ArrayList<PropositionalFunction>();
		
		PropositionalFunction isPlaneF = mb.pfIsPlaneF;
		PropositionalFunction isPlaneB = mb.pfIsPlaneB;
		PropositionalFunction isPlaneR = mb.pfIsPlaneR;
		PropositionalFunction isPlaneL = mb.pfIsPlaneL;
		
		PropositionalFunction isTrenchF = mb.pfIsTrenchF;
		PropositionalFunction isTrenchB = mb.pfIsTrenchB;
		PropositionalFunction isTrenchR = mb.pfIsTrenchR;
		PropositionalFunction isTrenchL = mb.pfIsTrenchL;
		
		predicates.add(isPlaneF);
		predicates.add(isPlaneB);
		predicates.add(isPlaneR);
		predicates.add(isPlaneL);
		
		predicates.add(isTrenchF);
		predicates.add(isTrenchB);
		predicates.add(isTrenchR);
		predicates.add(isTrenchL);
		
		
		KnowledgeBase<Affordance> affKnowlBase = generateAffordanceKB(predicates, lgds, allActions);
		
		// Initialize Learner
		AffordanceLearner affLearn = new AffordanceLearner(mb, mcsg, affKnowlBase, lgds);
		
		affLearn.learn();
		affLearn.printCounts();
		

		affKnowlBase.save("trenches" + affLearn.numWorldsPerLGD + ".kb");
	}

}
