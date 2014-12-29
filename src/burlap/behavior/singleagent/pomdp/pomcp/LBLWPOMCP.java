package burlap.behavior.singleagent.pomdp.pomcp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;




import burlap.behavior.singleagent.QValue;
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

public class LBLWPOMCP extends MonteCarloPOMDPPlanner{
	
	protected WeightedMonteCarloNode root = null;


	private int NUM_PARTICLES = 16;
	private double EPSILON = 1E-2;
	private double EXP_BONUS = 5;
	private int NUM_SIMS = NUM_PARTICLES;
	private int HORIZON = 88; // 88 steps with 0.95 as gamma and 0.01 as epsilon
	private int BRANCHING = 8;
	
	private Random randomNumber = RandomFactory.getMapped(0);
	
	public LBLWPOMCP(PODomain domain, RewardFunction rf, TerminalFunction tf, double discount, StateHashFactory hashingFactory, int inputHorizon, double explorationBonus, int inputNumParticles, int branching){
		super();
		this.EXP_BONUS=explorationBonus;
		this.HORIZON = inputHorizon;
		this.NUM_PARTICLES = inputNumParticles;
		this.NUM_SIMS = inputNumParticles;
		this.BRANCHING = branching;
		this.plannerInit(domain, rf, tf, discount, hashingFactory);
		root = new WeightedMonteCarloNode((PODomain)this.domain);
		
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
		BeliefState bs = new BeliefState((PODomain)this.domain);
		bs.setBeliefCollection(s.getFirstObjectOfClass(BeliefMDPGenerator.CLASSBELIEF).getDoubleArrayValue(BeliefMDPGenerator.ATTBELIEF));

		this.planFromBeliefStatistic(bs);
		if(this.root.hasChild((GroundedAction)a)){
			return new QValue(s, a, this.root.advance((GroundedAction)a).value);
		}
		return new QValue(s, a, 0.0);
	}

	@Override
	public GroundedAction getAction(State Observation,
			GroundedAction previousAction) {
		if(((PODomain)this.domain).getObservationFunction().isTerminalObservation(Observation)) {
			System.out.println("LBLWPOMCP: Domain returned a terminal observation, LWPOMCP returning a null action");
			return null;
		}

		updateRoot( previousAction, Observation);


		return this.getCurrentBestAction();
	}

	

	@Override
	public BeliefState getBeliefState() {
		return this.root.getCompleteBelief();
	}

	@Override
	public GroundedAction getCurrentBestAction() {
		return this.root.bestRealAction();
	}

	@Override
	public void planFromBeliefStatistic(BeliefStatistic bs) {
		root = new WeightedMonteCarloNode((PODomain)this.domain);


		for(int i=0; i < this.NUM_PARTICLES;i++){
			root.addParticle(bs.sampleStateFromBelief(),1);
		}
		
		int simulations = 0;
		root.normalizeWeights();
		while(simulations < this.NUM_SIMS) {
			simulations++;
			State s = root.sampleParticles();
			simulate(s, root, 0, null, null);
//			root.saveValues(); // this is for node explorer and might need a new one for weighted nodes
		}
		
		
	}

	@Override
	public void resetPlannerResults() {
		this.root.prune();
		this.root.clearBeliefCollection();
		this.root = null;
	}
	
	private void updateRoot(GroundedAction ga, State obs) {
		// TODO Auto-generated method stub
		
		WeightedMonteCarloNode parent = root;
		WeightedMonteCarloNode newRoot = new WeightedMonteCarloNode((PODomain)this.domain);
		
		
		if(root.advance(ga).advance(obs) == null) {
			root.advance(ga).addChild(obs);		
		}
		
		root.pruneExcept(ga);
		root.advance(ga).pruneExcept(obs);
		
		root = root.advance(ga).advance(obs);
		newRoot.setParticles(root.getParticles(),root.getWeights());
		root.prune();
		root = null;
		root = newRoot;
		
		// updating root particles
		while(root.particleCount() < this.NUM_PARTICLES) {
			
			State s = parent.sampleParticles();
//			System.out.println("parent particle: "+s.toString());
//			System.out.println("action: "+a.actionName());
			State s_ = ga.action.performAction(s, ga.params);
//			Observation o_ = domain.makeObservationFor(a, s_);
//			System.out.println("obs name: "+o_.getName());
//			System.out.println("out particle: "+s_.toString());
			root.addParticle(s_,obs,ga);
		}
		
		// doing simulations
		
		int simulations = 0;
		root.normalizeWeights();
		while(simulations < NUM_SIMS) {
			simulations++;
			State s = root.sampleParticles();
			simulate(s, root, 0, null, null);
//			root.saveValues(); // this is for node explorer and might need a new one for weighted nodes
		}
		
	}
	
	
	private double simulate(State state, WeightedMonteCarloNode node, int depth, State _o, GroundedAction _ga) {
//		System.out.println("was in simulate, node leaf value: " + node.isLeaf()+" is terminal: "+isTerminal(state));
//		if(isTerminal(state)){
//			System.out.println("State: " + state.toString());
//		}
//		if(Math.pow(this.GAMMA, depth) < this.EPSILON || isTerminal(state)) return 0;
		
		if(Math.pow(this.gamma, depth) < this.EPSILON ) return 0;
		if(_o!=null){
			if(((PODomain)this.domain).getObservationFunction().isTerminalObservation(_o)) return 0;
		}
		
//		if(!node.isLeaf()) {
//			System.out.println("This should not be printed at all");
//		}

		if(node.isLeaf()) {
			if(getGroundedActions(state).size() == 0) System.out.println("No actions for this state!");
//			System.out.println("This gets printed before each action");
			for(GroundedAction a : getGroundedActions(state)) {
				node.addChild(a);
			}

			double temp =  rollout(state, depth);
			return temp;
		}

		GroundedAction a = node.bestExploringAction(EXP_BONUS);
		State sPrime = (State) a.action.performAction(state, a.params);
		State o = ((PODomain)this.domain).getObservationFunction().sampleObservation(sPrime, a);
		double r = this.rf.reward(state, a, sPrime);

		if(!node.advance(a).hasChild(o)) {
			Map<HistoryElement,WeightedMonteCarloNode> children = node.advance(a).getMap();
			if(children.size()<=BRANCHING){
				node.advance(a).addChild(o);	
			}
			else{
				List<State> obsList = new ArrayList<State>();
				List<Double> obsProbabilityList = new ArrayList<Double>();
				List<Double> obsCDFList = new ArrayList<Double>();
				for(HistoryElement h : children.keySet()){
					State obsTemp = h.getObservation();
					obsList.add(obsTemp);
					obsProbabilityList.add(((PODomain)this.domain).getObservationFunction().getObservationProbability(obsTemp, sPrime, a));
				}
				Util.listNorm(obsProbabilityList);
				obsCDFList = Util.listCDF(obsProbabilityList);
				double tempRand = randomNumber.nextDouble();
				int ind=0;
				for(int count=0;count<obsCDFList.size();count++){
					if(tempRand < obsCDFList.get(count)){
						ind = count;
						break;
					}
				}
				o = null;
				o = obsList.get(ind);
			}
			
		}
		double expectedReward = r + this.gamma * simulate(sPrime, node.advance(a).advance(o), depth + 1, o , a);

		if(depth > 0 ) {
			node.addParticle(state,_o,_ga);
		}
		node.visit();
		node.advance(a).visit();
		node.advance(a).augmentValue((expectedReward - node.advance(a).getValue())/node.advance(a).getVisits());


		return expectedReward;
		
	}
	
	private double rollout(State state, int depth) {
		if(Math.pow(this.gamma, depth) < this.EPSILON || this.tf.isTerminal(state)) {
			return 0;}
		
		GroundedAction a = getGroundedActions(state).get(RandomFactory.getMapped(0).nextInt(getGroundedActions(state).size()));
		State sPrime = (State) a.action.performAction(state, a.params);
		

		double temp = this.rf.reward(state, a, sPrime) + this.gamma* rollout(sPrime, depth + 1);
		return temp;
	}
	
	private List<GroundedAction> getGroundedActions(State state) {
		List<GroundedAction> result = new ArrayList<GroundedAction>();
		for(Action a : domain.getActions()) {
			result.addAll(a.getAllApplicableGroundedActions(state));
		}
		return result;
	}

}
