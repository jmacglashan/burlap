/**
 * 
 */
package burlap.behavior.stochasticgame.agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.Policy.RandomPolicy;
import burlap.behavior.singleagent.auxiliary.performance.PerformanceMetric;
import burlap.behavior.singleagent.auxiliary.performance.TrialMode;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.stochasticgame.GameAnalysis;
import burlap.behavior.stochasticgame.GameSequenceVisualizer;
import burlap.behavior.stochasticgame.saconversion.ConversionGenerator;
import burlap.behavior.stochasticgame.saconversion.ExpectedPolicyWrapper;
import burlap.behavior.stochasticgame.saconversion.JointRewardFunctionWrapper;
import burlap.behavior.stochasticgame.saconversion.RandomSingleAgentPolicy;
import burlap.behavior.stochasticgame.saconversion.SGActionWrapper;
import burlap.domain.singleagent.gridworld.GridWorldStateParser;
import burlap.domain.stochasticgames.gridgame.GGVisualizer;
import burlap.domain.stochasticgames.gridgame.GridGame;
import burlap.domain.stochasticgames.gridgame.GridGameStandardMechanics;
import burlap.oomdp.auxiliary.common.StateYAMLParser;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.stochasticgames.Agent;
import burlap.oomdp.stochasticgames.AgentType;
import burlap.oomdp.stochasticgames.GroundedSingleAction;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.JointActionModel;
import burlap.oomdp.stochasticgames.JointReward;
import burlap.oomdp.stochasticgames.SGDomain;
import burlap.oomdp.stochasticgames.SGStateGenerator;
import burlap.oomdp.stochasticgames.SingleAction;
import burlap.oomdp.stochasticgames.World;
import burlap.oomdp.stochasticgames.WorldObserver;
import burlap.oomdp.stochasticgames.common.ConstantSGStateGenerator;
import burlap.oomdp.visualizer.Visualizer;

/**
 * 
 * This agent takes in a set of opponent agent policies and a distribution over those 
 * opponent agents and returns a best response policy that assumes a different 
 * opponent is chosen, from the given distribution, at each step.
 * 
 * This agent assumes there is a cognitive hierarchy of agents it is 
 * playing against. For more information, see [Wunder, Littman and Stone, 2009]
 * 
 * @author Betsy Hilliard (betsy@cs.brown.edu)
 *
 */
public class BestResponseToDistributionAgent extends Agent {

	private boolean isFirstDay = false;
	private boolean CHOOSE = false;

	protected SADomain singleAgentDomain;
	protected Map<String, Policy> otherAgentPolicies;
	protected Map<String, Map<Integer,Policy>> allOtherAgentPolicies;
	protected Map<String, Map<Integer,Double>> distributionOverAllOtherAgentPolicies;
	/**
	 * The policy this agent follows
	 */
	protected Policy									policy;

	protected StateHashFactory							hashFactory;


	/**
	 * The Agent class is
	 */
	public BestResponseToDistributionAgent(SGDomain domain, Map<String, Map<Integer,Policy>> allOtherAgentPolicies, 
			Map<String, Map<Integer,Double>> distributionOverAllOtherAgentPolicies, StateHashFactory hashFactory) {

		this.allOtherAgentPolicies = allOtherAgentPolicies;
		this.distributionOverAllOtherAgentPolicies = distributionOverAllOtherAgentPolicies;
		this.hashFactory = hashFactory;
		this.domain = domain;
		if(CHOOSE){
			otherAgentPolicies = chooseOtherAgentPolicies();
		}else{
			otherAgentPolicies = constructOtherAgentPolicies();
		}

	}


	/*
	 * this picks based on the distribution
	 * 
	 * TODO: combine the policies based on the distribution
	 */
	private Map<String, Policy> chooseOtherAgentPolicies() {
		Random rand = new Random();

		Map<String,Policy> policyMap = new HashMap<String,Policy>();
		for(String otherAgentName : allOtherAgentPolicies.keySet()){
			double draw = rand.nextDouble();
			double total = 0.0;
			Integer levelChosen = null;
			for(Integer level: distributionOverAllOtherAgentPolicies.get(otherAgentName).keySet()){
				if(distributionOverAllOtherAgentPolicies.get(otherAgentName).get(level)+total>draw && levelChosen==null){
					levelChosen = level;
				}else{
					total = total+distributionOverAllOtherAgentPolicies.get(otherAgentName).get(level);
				}
			}

			policyMap.put(otherAgentName,allOtherAgentPolicies.get(otherAgentName).get(levelChosen));
		}
		return policyMap;
	}
	/*
	 * this combines the policies into one per 
	 * other agent based on the distribution
	 * 
	 * TODO: combine the policies based on the distribution
	 */
	private Map<String, Policy> constructOtherAgentPolicies() {


		Map<String,Policy> policyMap = new HashMap<String,Policy>();
		for(String otherAgentName : allOtherAgentPolicies.keySet()){
			System.out.println("Other Agent Name: "+otherAgentName);
			Policy newPolicy = new ExpectedPolicyWrapper(allOtherAgentPolicies.get(otherAgentName),
					distributionOverAllOtherAgentPolicies.get(otherAgentName));

			policyMap.put(otherAgentName, newPolicy);
		}
		return policyMap;
	}

	/* (non-Javadoc)
	 * @see burlap.oomdp.stochasticgames.Agent#gameStarting()
	 */
	@Override
	public void gameStarting() {

		//set flag here so when given first state can plan
		isFirstDay = true;

	}

	/* (non-Javadoc)
	 * @see burlap.oomdp.stochasticgames.Agent#getAction(burlap.oomdp.core.State)
	 */
	@Override
	public GroundedSingleAction getAction(State s) {

		
		if(isFirstDay){
			//plan and create policy
			
			ConversionGenerator generator = new ConversionGenerator(domain, world.getActionModel(), 
					agentType, worldAgentName, otherAgentPolicies, hashFactory);

			singleAgentDomain = (SADomain) generator.generateDomain();
			
			//List<TransitionProbability> tps = singleAgentDomain.getAction(GridGame.ACTIONNORTH).getTransitions(s, "");
			
			//System.out.println("Size tps: "+tps.size());
			
			//System.exit(0);

			RewardFunction rf = new JointRewardFunctionWrapper(world.getRewardModel(), getAgentName(), domain, 
					otherAgentPolicies, world.getActionModel());

			policy = valueIteration("/Users/betsy/research/cognitive_hierarchy/testOut.txt", rf, singleAgentDomain);
			//reset isFirstDay
			isFirstDay = false;
		}
		//change single action to multi agent action
		//return from policy generated at beginning
		
		//GroundedAction to GroundedSingleAction
		AbstractGroundedAction ga = policy.getAction(s);
		
		List<GroundedSingleAction> gsas = domain.getSingleAction(ga.actionName()).getAllGroundedActionsFor(s, worldAgentName);
		Random rand = new Random();
		GroundedSingleAction gsa = gsas.get(rand.nextInt(gsas.size()));
		return gsa;


	}

	/* (non-Javadoc)
	 * @see burlap.oomdp.stochasticgames.Agent#observeOutcome(burlap.oomdp.core.State, burlap.oomdp.stochasticgames.JointAction, java.util.Map, burlap.oomdp.core.State, boolean)
	 */
	@Override
	public void observeOutcome(State s, JointAction jointAction,
			Map<String, Double> jointReward, State sprime, boolean isTerminal) {


		/*
		 * may not need to do anything here because we don't update the agent while
		 * the agent is playing
		 */


	}

	/* (non-Javadoc)
	 * @see burlap.oomdp.stochasticgames.Agent#gameTerminated()
	 */
	@Override
	public void gameTerminated() {
		// TODO Auto-generated method stub

	}

	public Policy valueIteration(String outputPath, RewardFunction rf, Domain saDomain){

		if(!outputPath.endsWith("/")){
			outputPath = outputPath + "/";
		}

		//TODO: fix here!!!!!!!!!
		OOMDPPlanner planner = new ValueIteration(saDomain, rf, world.getTF(), 0.99, hashFactory, 0.001,100000);

		planner.planFromState(world.getCurrentWorldState());
		

		//create a Q-greedy policy from the planner
		Policy p = new GreedyQPolicy((QComputablePlanner)planner);

		GridWorldStateParser gwsp = new GridWorldStateParser(saDomain);
		//record the plan results to a file
		//System.out.println("Before eval");
		System.out.println(p.evaluateBehavior(world.getCurrentWorldState(), rf, world.getTF(), 100).getActionSequenceString("\n")); //writeToFile(outputPath + "planResult", gwsp);
		return p;
	}

	public static void main(String[] args){
		GridGame gg = new GridGame();

		Agent oponent = new RandomAgent();

		SGDomain d = (SGDomain)gg.generateDomain();
		
		//AgentType at = new AgentType(oponent.getAgentName(), d.getActions());

		//State s = GridGame.getCorrdinationGameInitialState(d);
		State s = GridGame.getTurkeyInitialState(d);

		//System.out.println(s.getCompleteStateDescription());

		JointActionModel jam = new GridGameStandardMechanics(d);
		d.setJointActionModel(jam);
		
		JointReward jr = new GridGame.GGJointRewardFunction(d, -1, 100.0, 100.0, true);
		TerminalFunction tf = new GridGame.GGTerminalFunction(d);
		SGStateGenerator sg = new ConstantSGStateGenerator(s);

		World gameWorld = new World(d, jr, tf, sg);

		//oponent.joinWorld(gameWorld, oponent.getAgentType());
		oponent.joinWorld(gameWorld, new AgentType(GridGame.CLASSAGENT, d.getObjectClass(GridGame.CLASSAGENT), d.getSingleActions()));

		String oponentName = oponent.getAgentName();

		Map<String, Map<Integer,Policy>> allOtherAgentPolicies = new HashMap<String, Map<Integer,Policy>>();
		HashMap<Integer, Policy> levelMap = new HashMap<Integer, Policy>();
		
		List<SingleAction> actions = d.getSingleActions();
		
		RandomSingleAgentPolicy randPolicy = new RandomSingleAgentPolicy(oponent.getAgentName(), actions);
		levelMap.put(0, randPolicy);
		allOtherAgentPolicies.put(oponentName, levelMap);


		Map<String, Map<Integer,Double>> distributionOverAllOtherAgentPolicies  = new HashMap<String, Map<Integer,Double>>();
		HashMap<Integer,Double> distribution = new HashMap<Integer,Double>();
		distribution.put(0, 1.0);
		distributionOverAllOtherAgentPolicies.put(oponent.getAgentName(),distribution);

		StateHashFactory hashFactory = new DiscreteStateHashFactory();

		BestResponseToDistributionAgent brAgent = new BestResponseToDistributionAgent(d,allOtherAgentPolicies,
				distributionOverAllOtherAgentPolicies, hashFactory);

		brAgent.joinWorld(gameWorld, new AgentType(GridGame.CLASSAGENT, d.getObjectClass(GridGame.CLASSAGENT), d.getSingleActions()));

	
		
		//gameWorld.addWorldObserver(ob);
		System.out.println("running game");
		GameAnalysis ga = gameWorld.runGame();
		List<GameAnalysis> gas = new ArrayList<GameAnalysis>();
		gas.add(ga);
		
		Visualizer v = GGVisualizer.getVisualizer(5, 6);
		
		GameSequenceVisualizer gsv = new GameSequenceVisualizer(v,d,gas);
		
		System.out.println("BR Agent Name: "+brAgent.getAgentName());
		
		//System.out.println(ga.parseIntoString(new StateYAMLParser(d)));
		//gsv.initGUI();
		
		//System.out.println("Reward l1: "+gameWorld.getCumulativeRewardForAgent(brAgent.getAgentName()));
		//System.out.println("Reward l0: "+gameWorld.getCumulativeRewardForAgent(oponent.getAgentName()));
		//System.out.println("game finished");

	}

}
