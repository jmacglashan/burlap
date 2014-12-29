package burlap.behavior.singleagent.pomdp.pomcp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;







import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.auxiliary.StateEnumerator;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.debugtools.RandomFactory;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.pomdp.BeliefMDPGenerator;
import burlap.oomdp.singleagent.pomdp.BeliefState;
import burlap.oomdp.singleagent.pomdp.BeliefStatistic;
import burlap.oomdp.singleagent.pomdp.PODomain;


// coded like a Q computable planner where it return a Q network 
//when given a belief state, but we give an update method so that the 
//next step is generated with the update method.

/**
 * An implementation of the POMCP solver from "Monte-Carlo Planning in Large POMDPs", 
 * by David Silver and Joel Veness
 * The code works both as a Qcomputable planner and that of a greedy policy class
 * @author ng
 *
 */
public class POMCP extends MonteCarloPOMDPPlanner{
	protected MonteCarloNode root = null;

	private int NUM_PARTICLES = 128;
	//	private long TIME_ALLOWED = 500;
	//	private double GAMMA = 0.95;
	private double EPSILON = 1E-2;
	private double EXP_BONUS = 20;
	private int NUM_SIMS = NUM_PARTICLES;
	private int HORIZON = 88;// 88 steps for .95 to be less than Epsilon = 0.01
	private Random rand = RandomFactory.getMapped(0);
	private boolean obsExists = true;
	private List<GroundedAction> gaList = new ArrayList<GroundedAction>();
	
	
	private int countRandom = 0;

	public POMCP(PODomain domain, RewardFunction rf, TerminalFunction tf, double discount, StateHashFactory hashingFactory, int inputHorizon, double explorationBonus, int inputNumParticles){
		super();
		this.EXP_BONUS=explorationBonus;
		this.HORIZON = inputHorizon;
		this.NUM_PARTICLES = inputNumParticles;
		this.NUM_SIMS = inputNumParticles;
		this.plannerInit(domain, rf, tf, discount, hashingFactory);
		initPOMCP();
	}



	private void initPOMCP(){
		root = new MonteCarloNode((PODomain)this.domain);
	}


	@Override
	public List<QValue> getQs(State s) {
		BeliefState bs = new BeliefState((PODomain)this.domain);
		bs.setBeliefCollection(s.getFirstObjectOfClass(BeliefMDPGenerator.CLASSBELIEF).getDoubleArrayValue(BeliefMDPGenerator.ATTBELIEF));
		
		this.planFromBeliefStatistic(bs);
		
		return this.root.returnQVlaueForNode(s);
	}

	@Override
	public QValue getQ(State s, AbstractGroundedAction a) {
		// check child to see if action present if yes get the action's value
		
		BeliefState bs = new BeliefState((PODomain)this.domain);
		bs.setBeliefCollection(s.getFirstObjectOfClass(BeliefMDPGenerator.CLASSBELIEF).getDoubleArrayValue(BeliefMDPGenerator.ATTBELIEF));
		
		this.planFromBeliefStatistic(bs);
		if(this.root.hasChild((GroundedAction)a)){
			return new QValue(s, a, this.root.advance((GroundedAction)a).value);
		}
		return new QValue(s, a, 0.0);
	}

	@Override
	public void planFromBeliefStatistic(BeliefStatistic bs) {
		root = new MonteCarloNode((PODomain)this.domain);
		// fill the root node up with sample particles
		

		for(int i=0; i < this.NUM_PARTICLES;i++){
			root.addParticle(bs.sampleStateFromBelief());
		}
		
		this.gaList.clear();
		this.gaList = getGroundedActions(root.sampleParticles());
		
		// do simulations over the particles to get values and best actions
		int simulations = 0; 

		while((simulations < NUM_SIMS) && obsExists) {
			simulations++;
			State s = root.sampleParticles();
			simulate(s, root, 0);
			root.saveValues();
		}

	}

	
	/*
	 * 
	 * @param s - current belief state that has been queried from the planner
	 * @param Observation
	 * @param previousAction
	 * @return
	 */
	/*
	public List<QValue> updateAndGetQs(State s, State Observation, GroundedAction previousAction){
		//TODO: this is not clean
		if(((PODomain)this.domain).getObservationFunction().isTerminalObservation(Observation)) {
			System.out.println("Domain returned a terminal observation, POMCP returning a null action");
			return null;
		}

		updateRoot(previousAction, Observation);

		return this.root.returnQVlaueForNode(s);
	}
	
	*/

	@Override
	public void resetPlannerResults() {
		// deleting all children nodes and clearing out the root itself
		this.root.prune();
		this.root.clearBeliefCollection();
		this.root = null;
	}



	@Override
	public BeliefState getBeliefState() {
		return this.root.getCompleteBelief();
	}



	@Override
	public GroundedAction getAction(State Observation,
			GroundedAction previousAction) {
		// this updates the root and gets the next best action
		if(((PODomain)this.domain).getObservationFunction().isTerminalObservation(Observation)) {
			System.out.println("POMCP: Domain returned a terminal observation, POMCP returning a null action");
			return null;
		}

		updateRoot( previousAction, Observation);


		return this.getCurrentBestAction();
	}



	@Override
	public GroundedAction getCurrentBestAction() {
		if(this.obsExists){
			return this.root.bestRealAction();
		}
		//TODO:  return a random action possible from the environment
		//State s = root.sampleParticles();
//		countRandom++;
//		System.out.println("picking random actions: " + countRandom);
		return this.gaList.get(rand.nextInt(this.gaList.size()));
	}


	

	protected void updateRoot(GroundedAction ga, State obs){
		//This updates the root node with the action and observation performed
//		System.out.println(ga.actionName() + obs.getStateDescription());


		MonteCarloNode parent = root;
		MonteCarloNode newRoot = new MonteCarloNode((PODomain)this.domain);
		
		// update particles to the new root
		if(obsExists){
//			
//			System.out.println("visits:"+  root.advance(ga).visits + " children size " +root.advance(ga).children.size());
//			Map<HistoryElement, MonteCarloNode> testMap = root.advance(ga).getMap();
//			System.out.println("observations: ");
//			for(HistoryElement h : testMap.keySet()){
//				System.out.println(h.getObservation().getCompleteStateDescription());
//			}
			

			if(root.advance(ga).advance(obs) == null) {
				obsExists = false;
				root.advance(ga).addChild(obs);		
			}
			root.pruneExcept(ga);
			root.advance(ga).pruneExcept(obs);
			root = root.advance(ga).advance(obs);
			//			System.out.println("RootValue before:" + root.getVisits());
			newRoot.setParticles(root.getParticles());
			root.prune();
			root = null;

			root = newRoot;
			//			System.out.println("RootValue:" + root.getVisits());

			//long timeStart = System.currentTimeMillis();

			while(this.obsExists && root.particleCount() < this.NUM_PARTICLES) {

				State s = parent.sampleParticles();
				State s_ = ga.action.performAction(s, ga.params);
				State o_ = ((PODomain)domain).getObservationFunction().sampleObservation(s_, ga);
				if(compareObservations(obs, o_)) root.addParticle(s_);
			}
			while(root.particleCount() > this.NUM_PARTICLES) {
				root.removeRandomParticle();
			}
		}
		
		// do simulations on the new root to get values
		int simulations = 0; 
		while((simulations < this.NUM_SIMS) && this.obsExists) {
			simulations++;
			State s = root.sampleParticles();
			simulate(s, root, 0);
			root.saveValues();
		}
		




	}

	protected double simulate(State state, MonteCarloNode node, int depth) {
		//		System.out.println("POMCPSolver: in simulate");
		if(Math.pow(this.gamma, depth) < this.EPSILON || this.tf.isTerminal(state)) return 0;

		if(node.isLeaf()) {
			if(getGroundedActions(state).size() == 0) System.out.println("No actions for this state!");
			for(GroundedAction a : getGroundedActions(state)) {
				node.addChild(a);
			}

			double temp =  rollout(state, depth);
			//			System.out.println("POMCPSolver: out of simulate");
			return temp;
		}

		GroundedAction a = node.bestExploringAction(EXP_BONUS);
		State sPrime = (State) a.action.performAction(state, a.params);
		State o = ((PODomain)this.domain).getObservationFunction().sampleObservation(sPrime, a);
		double r = this.rf.reward(state, a, sPrime);

		if(!node.advance(a).hasChild(o)) node.advance(a).addChild(o);
		double expectedReward = r + this.gamma * simulate(sPrime, node.advance(a).advance(o), depth + 1);

		if(depth > 0) node.addParticle(state);
		node.visit();
		node.advance(a).visit();
		node.advance(a).augmentValue((expectedReward - node.advance(a).getValue())/node.advance(a).getVisits());
		//		System.out.println("POMCPSolver: out of simulate");

		return expectedReward;

	}

	private double rollout(State state, int depth) {
		//		System.out.println("POMCPSolver: in rollout");
		if(Math.pow(this.gamma, depth) < this.EPSILON || this.tf.isTerminal(state)) {
			//			System.out.println("POMCPSolver: in simulate");
			return 0;
			}

		//		GroundedAction a = getGroundedActions(state).get(new java.util.Random().nextInt(getGroundedActions(state).size()));
		GroundedAction a = getGroundedActions(state).get(rand.nextInt(getGroundedActions(state).size()));
		State sPrime =  a.action.performAction(state, a.params);



		double temp = this.rf.reward(state, a, sPrime) + this.gamma * rollout(sPrime, depth + 1);
		//		System.out.println("POMCPSolver: out of rollout");
		return temp;
	}

	private List<GroundedAction> getGroundedActions(State state) {
		//		System.out.println("POMCPSolver: in get grounded actions");
		List<GroundedAction> result = new ArrayList<GroundedAction>();
		for(Action a : this.domain.getActions()) {
			//			result.addAll(state.getAllGroundedActionsFor(a));
			result.addAll(a.getAllApplicableGroundedActions(state));
		}
		//		System.out.println("POMCPSolver: out of get grounded actions");
		return result;
	}

	private boolean compareObservations(State o1, State o2) {
		return o1.equals(o2);
	}

}
