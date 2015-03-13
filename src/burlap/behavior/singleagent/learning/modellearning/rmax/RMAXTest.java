package burlap.behavior.singleagent.learning.modellearning.rmax;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import minecraft.NameSpace;
import minecraft.MinecraftBehavior.MinecraftBehavior;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.auxiliary.StateReachability;
import burlap.behavior.singleagent.auxiliary.performance.LearningAlgorithmExperimenter;
import burlap.behavior.singleagent.auxiliary.performance.TrialMode;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.ValueFunctionVisualizerGUI;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.behavior.singleagent.learning.modellearning.Model;
import burlap.behavior.singleagent.learning.modellearning.ModelPlanner.ModelPlannerGenerator;
import burlap.behavior.singleagent.learning.modellearning.modelplanners.VIModelPlanner;
import burlap.behavior.singleagent.learning.modellearning.models.TabularModel;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.ConditionHypothesis;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.PredictionsLearner;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.OOMDPModel;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects.Effect;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects.EffectHelpers;
import burlap.behavior.singleagent.learning.modellearning.models.PerceptualModel.FullAttributeStatePerception;
import burlap.behavior.singleagent.planning.ValueFunctionPlanner;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy;
import burlap.behavior.singleagent.shaping.potential.PotentialFunction;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.domain.singleagent.gridworld.GridWorldTerminalFunction;
import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.auxiliary.StateGenerator;
import burlap.oomdp.auxiliary.common.ConstantStateGenerator;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.common.UniformCostRF;





public class RMAXTest {

	private static List<State> getUnmodelledStates(State initialState, Domain d, Model model) {
		PrintStream originalStream = System.out;
		
		OutputStream dummyStreamO = new OutputStream(){
		    public void write(int b) {
		        //NO-OP
		    }
		};

		
		System.setOut(new PrintStream(dummyStreamO));
		List<State> allStates = StateReachability.getReachableStates(initialState, (SADomain)d, new DiscreteStateHashFactory());

		System.setOut(originalStream);

		List<State> unmodeledStates = new ArrayList<State>();
		for (State state : allStates) {
			if (!(model.stateTransitionsAreModeled(state))) {
				unmodeledStates.add(state);
			}
		}
		return unmodeledStates;
	}



	public static class WallRF implements RewardFunction{

		int gx;
		int gy;

		public WallRF(int gx, int gy){
			this.gx = gx;
			this.gy = gy;
		}


		@Override
		public double reward(State s, GroundedAction a, State sprime) {

			ObjectInstance nagent = sprime.getFirstObjectOfClass(GridWorldDomain.CLASSAGENT);
			int nx = nagent.getDiscValForAttribute(GridWorldDomain.ATTX);
			int ny = nagent.getDiscValForAttribute(GridWorldDomain.ATTY);

			//did agent reach goal location?
			if(nx == this.gx && ny == this.gy){
				return 1000.;
			}

			ObjectInstance pagent = s.getFirstObjectOfClass(GridWorldDomain.CLASSAGENT);
			int px = pagent.getDiscValForAttribute(GridWorldDomain.ATTX);
			int py = pagent.getDiscValForAttribute(GridWorldDomain.ATTY);

			//if agent didn't change position, they must have hit a wall
			if(px == nx && py == ny){
				return -100.;
			}

			return -1.;
		}
	}


	public static void main(String[] args) {

		//PARAMS TO SET
		boolean useRmax = false; //if false uses DOORMAX

		boolean gridWorld = false;
		boolean taxi = true;
		boolean minecraft = false;

		boolean learnWithRandPolicyFirst = true;
		int randIterations = 201;
		int randActionsPerIteration = 500;

		boolean runLearningWithModel = false;
		int learningIterations = 5;
		int actionsPerIteration = 50;


		final int nConfident = 1;
		final double maxVIDelta = .1;
		final int maxVIPasses = 10;

		int k = 5;

		//NULL PARAMS
		final DiscreteStateHashFactory hf = new DiscreteStateHashFactory();

		final double maxReward = 0;
		Domain d = null;
		State initialState = null;
		TerminalFunction tf = null;
		RewardFunction rf = null;
		PotentialShapedRMax rmax = null;
		Model model = null;

		StringBuilder toWriteToFile = new StringBuilder();
		//GRID WORLD DOMAIN
		//Set up domain and initial state
		if (gridWorld) {
			GridWorldDomain gwdg = new GridWorldDomain(11, 11);
			gwdg.setMapToFourRooms();

			int endX = 10;
			int endY = 10;

			d = gwdg.generateDomain();
			initialState = GridWorldDomain.getOneAgentOneLocationState(d);
			GridWorldDomain.setAgent(initialState, 0, 0);
			GridWorldDomain.setLocation(initialState, 0, endX, endY, 0);

			rf = new UniformCostRF();

			tf = new GridWorldTerminalFunction(10, 10);

		}

		//MINECRAFT DOMAIN
		if (minecraft) {
			String mapName = "src/plane1.map";

			MinecraftBehavior mcBeh = new MinecraftBehavior(mapName);
			d = mcBeh.getDomain();
			rf = mcBeh.getRewardFunction();
			tf = mcBeh.getTerminalFunction();
			initialState = mcBeh.getInitialState();
			rf = new UniformCostRF();

			List<State> allReachableStates= StateReachability.getReachableStates(initialState, (SADomain) d, hf);
			for (State state : allReachableStates) {
				if (state.getAllUnsetAttributes().keySet().size() != 0) {
					System.out.println(state.getAllUnsetAttributes());
				}
			}
		}
		//TAXI DOMAIN
		if (taxi) {
			final DomainGenerator dg = new TaxiDomain();
			d = dg.generateDomain();
			initialState = TaxiDomain.getClassicState(d);
			tf = new TaxiDomain.TaxiTF();
			rf = new UniformCostRF();
		}
		//BFS
		//		TFGoalCondition goalCondition = new TFGoalCondition(tf);

		//		DeterministicPlanner planner = new BFS(d, goalCondition, new DiscreteStateHashFactory());
		//		
		//		planner.planFromState(initialState);
		//		
		//		Policy p = new SDPlannerPolicy(planner);
		//		
		//		
		//		EpisodeAnalysis ea = p.evaluateBehavior(initialState, rf, tf);
		//		System.out.println(ea.getActionSequenceString());



		//RMAX
		if (useRmax) {
			model = new TabularModel(d,new DiscreteStateHashFactory(),1);
		}
		//DOORMAX
		else {
			List<PropositionalFunction> propFunsToUse = d.getPropFunctions();
			List<String> effectsToUse = new ArrayList<String>();
			effectsToUse.add(EffectHelpers.arithEffect);
			effectsToUse.add(EffectHelpers.assigEffect);
			model = new OOMDPModel(d,rf, tf,propFunsToUse, effectsToUse, initialState, k);
		}
		//RUN ALGORITHM
		//random policy
		if (learnWithRandPolicyFirst) {

			for(int i = 0; i < randIterations; i++){
				State currState = initialState.copy();
				Policy randPol = new Policy.RandomPolicy(d);
				List<State> unmodeledStates = getUnmodelledStates(initialState, d, model);

				toWriteToFile.append(randActionsPerIteration*i + "\t" + unmodeledStates.size() + "\n");
				for (int j = 0; j < randActionsPerIteration; j++) {

					GroundedAction ga = (GroundedAction) randPol.getAction(currState);
					State sprime = ga.executeIn(currState);
					double r = rf.reward(currState, ga, sprime);
					boolean sprimeIsTerminal = tf.isTerminal(sprime);
					model.updateModel(currState, ga, sprime, r, sprimeIsTerminal);
					currState = sprime;

					FullAttributeStatePerception test = new FullAttributeStatePerception(currState, "BLAH");
					
					System.out.println(test.getArffString(true));

				}




			}
		}

		//VI policy
		if (runLearningWithModel) {
			rmax = new PotentialShapedRMax(d, rf, tf,.9, hf,maxReward, nConfident, maxVIDelta, maxVIPasses,  model);
			for (int i = 0; i < learningIterations; i ++) {
				List<State> unmodeledStates = getUnmodelledStates(initialState, d, rmax.getModel());

				toWriteToFile.append(randActionsPerIteration*i + "\t" + unmodeledStates.size() + "\n");

				EpisodeAnalysis ea = rmax.runLearningEpisodeFrom(initialState, actionsPerIteration);
				System.out.println(ea.actionSequence);


//				System.out.println("Num unmodelled states: " + unmodeledStates.size());
			}
		}


		//PRINT INCORRECT PREDICTIONS
		//		System.out.println("INCORRECT PREDICTIONS: ");
		//		List<State> allStatesT = StateReachability.getReachableStates(initialState, (SADomain)d, new DiscreteStateHashFactory());
		//		for (State s : allStatesT) {
		//			for (Action a : d.getActions()) {
		//				for (GroundedAction ga : a.getAllApplicableGroundedActions(s)) {
		//					ConditionHypothesis currStateCondition = new ConditionHypothesis(s, propFunsToUse);
		//					State sPrime = ga.executeIn(s);
		//					List<Effect> predictedEffects = oomdpModel.getPredictionsLearner().predictEffects(s, ga);
		//					State predictedState = oomdpModel.sampleModelHelper(s, ga);
		//					
		//					if (!predictedState.equals(sPrime) && predictedEffects != null) {
		//						System.out.println("Incorrect prediction of " + ga + " on " + s);
		//					}
		//				}
		//			}
		//		}


		//PRINT NUM UNLEARNED STATES and PREDICTIONS LEARNER

		System.out.println("Num unmodeled states: " + getUnmodelledStates(initialState, d, model).size());
		if (!useRmax) {
			System.out.println(((OOMDPModel) model).getPredictionsLearner());
		}

		//GRID WORLD VISUALIZER
		//get reachable states
		if (gridWorld && rmax != null) {

			ValueFunctionPlanner planner = ((VIModelPlanner)rmax.getModelPlanner()).getValueIterationPlanner().getCopyOfValueFunction();

			List<State> allStates = StateReachability.getReachableStates(initialState, (SADomain)d, new DiscreteStateHashFactory());

			List<State> modeledStates = new ArrayList<State>();
			for (State state : allStates) {
				if (model.stateTransitionsAreModeled(state)) {
					modeledStates.add(state);
				}
			}
			ValueFunctionVisualizerGUI gui = GridWorldDomain.getGridWorldValueFunctionVisualization(modeledStates, planner, new GreedyQPolicy(planner));
			gui.initGUI();
		}

		//TAXI VISUALIZER
		if (taxi) {




		}
		System.out.println(toWriteToFile);

	}


	//TODO:
	/**
	 * Make efficient
	 * Don't count on state equality -- use hashing factories
	 */


}
