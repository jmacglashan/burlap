/**
 * 
 */
package burlap.behavior.stochasticgame.agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.ValueFunctionInitialization;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.behavior.singleagent.planning.stochastic.rtdp.BoundedRTDP;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.stochasticgame.GameAnalysis;
import burlap.behavior.stochasticgame.GameSequenceVisualizer;
import burlap.behavior.stochasticgame.saconversion.ConversionGenerator;
import burlap.behavior.stochasticgame.saconversion.ExpectedPolicyWrapper;
import burlap.behavior.stochasticgame.saconversion.JointRewardFunctionWrapper;
import burlap.behavior.stochasticgame.saconversion.MinDistValueFunctionInitialization;
import burlap.behavior.stochasticgame.saconversion.RTDPGreedyQPolicy;
import burlap.behavior.stochasticgame.saconversion.RandomSingleAgentPolicy;
import burlap.domain.singleagent.gridworld.GridWorldStateParser;
import burlap.domain.stochasticgames.gridgame.GGVisualizer;
import burlap.domain.stochasticgames.gridgame.GridGame;
import burlap.domain.stochasticgames.gridgame.GridGameStandardMechanics;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
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


	private boolean CHOOSE = false; //setting this to true means that an agent type will be chosen randomly???
									// This really doesn't make sense, so don't set to true
	

	protected SADomain singleAgentDomain;
	protected Map<String, Policy> otherAgentPolicies;
	protected Map<String, Map<Integer,Policy>> allOtherAgentPolicies;
	protected Map<String, Map<Integer,Double>> distributionOverAllOtherAgentPolicies;

	protected OOMDPPlanner planner = null;
	private boolean isFirstDay = false;
	/**
	 * The policy this agent follows
	 */
	protected Policy									policy;

	protected StateHashFactory							hashFactory;
	
	private double 										goalReward;
	
	private boolean 									runValueIteration;


	/**
	 * The Agent class is
	 */
	public BestResponseToDistributionAgent(SGDomain domain, Map<String, Map<Integer,Policy>> allOtherAgentPolicies, 
			Map<String, Map<Integer,Double>> distributionOverAllOtherAgentPolicies, StateHashFactory hashFactory, double goalReward) {

		this.allOtherAgentPolicies = allOtherAgentPolicies;
		this.distributionOverAllOtherAgentPolicies = distributionOverAllOtherAgentPolicies;
		this.hashFactory = hashFactory;
		this.domain = domain;
		this.goalReward = goalReward;
		if(CHOOSE){
			otherAgentPolicies = chooseOtherAgentPolicies();
		}else{
			otherAgentPolicies = constructOtherAgentPolicies();
		}

	}

	public BestResponseToDistributionAgent(SGDomain domain, StateHashFactory hashFactory, double goalReward, boolean runValueItteration) {
		this.hashFactory = hashFactory;
		this.domain = domain;
		this.goalReward = goalReward;
		this.runValueIteration = runValueItteration;

	}

	/**
	 * Takes as input the maps we create and sets the maps for this agent
	 * @param allOtherAgentPolicies
	 * @param distributionOverAllOtherAgentPolicies
	 */
	public void setOtherAgentPolicyMaps(Map<String, Map<Integer,Policy>> allOtherAgentPolicies, 
			Map<String, Map<Integer,Double>> distributionOverAllOtherAgentPolicies){

		this.allOtherAgentPolicies = allOtherAgentPolicies;
		this.distributionOverAllOtherAgentPolicies = distributionOverAllOtherAgentPolicies;

		if(CHOOSE){
			otherAgentPolicies = chooseOtherAgentPolicies();
		}else{
			otherAgentPolicies = constructOtherAgentPolicies();
		}

	}


	/**
	 * This picks an agent based on the distribution
	 * 
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
	
	/**
	 * this combines the policies into one per 
	 * other agent based on the distribution
	 * 
	 */
	private Map<String, Policy> constructOtherAgentPolicies() {


		Map<String,Policy> policyMap = new HashMap<String,Policy>();
		for(String otherAgentName : allOtherAgentPolicies.keySet()){
			//System.out.println("Other Agent Name: "+otherAgentName);
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

			RewardFunction rf = new JointRewardFunctionWrapper(world.getRewardModel(), getAgentName(), domain, 
					otherAgentPolicies, world.getActionModel(), domain.getSingleActions());

			if(runValueIteration){
				policy = valueIteration("/Users/betsy/research/cognitive_hierarchy/testOut.txt", rf, singleAgentDomain);
			}else{

				policy = boundedRTDP(singleAgentDomain, domain, this.getAgentName(),rf,.99, goalReward);
			}
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

	public Policy getPolicy(){
		return policy;
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

	public Policy boundedRTDP(Domain saDomain, Domain ggDomain, String agentName, RewardFunction rf, double gamma, double goalReward){
		ValueFunctionInitialization lowerVInit = new ValueFunctionInitialization.ConstantValueFunctionInitialization(-1.0/(1.0-gamma));
		ValueFunctionInitialization upperVInit = new MinDistValueFunctionInitialization(ggDomain, agentName, goalReward);
		double maxDiff = 100; //what should this be set to???
		int maxRollouts = 500; //what should this be set to???

		planner = new BoundedRTDP(saDomain, rf, world.getTF(), gamma, 
				hashFactory, lowerVInit, upperVInit, maxDiff, maxRollouts);

		planner.planFromState(world.getCurrentWorldState()); 

		Policy p = new RTDPGreedyQPolicy((QComputablePlanner)planner);

		return p;
	}

	public Policy valueIteration(String outputPath, RewardFunction rf, Domain saDomain){

		if(!outputPath.endsWith("/")){
			outputPath = outputPath + "/";
		}


		if(planner == null){
			System.out.println("creating planner");
			//should these parameters be pulled out?
			planner = new ValueIteration(saDomain, rf, world.getTF(), 0.99, hashFactory, 0.001,1000000);
		}

		System.out.println("VI starting");
		planner.planFromState(world.getCurrentWorldState());
		System.out.println("VI done planning");

		//create a Q-greedy policy from the planner
		Policy p = new GreedyQPolicy((QComputablePlanner)planner);

		GridWorldStateParser gwsp = new GridWorldStateParser(saDomain);
		//record the plan results to a file
		//System.out.println("Before eval");
		//System.out.println(p.evaluateBehavior(world.getCurrentWorldState(), rf, world.getTF(), 100).getActionSequenceString("\n")); //writeToFile(outputPath + "planResult", gwsp);

		return p;
	}

	//this is an old method used before Experiment Runner was created.
	public static void main(String[] args){
		GridGame gg = new GridGame();

		Agent oponent = new RandomAgent();
		Policy previousPolicy =null;

		List<GameAnalysis> gas = new ArrayList<GameAnalysis>();
		SGDomain d = (SGDomain)gg.generateDomain();

		Map<String,Map<Integer, Policy>> brAgentPolicies = new HashMap<String,Map<Integer, Policy>>();
		boolean OTHERFIRST = false; //true;

	
		for(int k = 0;k<=1;k++){
			System.out.println("LEVEL: "+k);


			//AgentType at = new AgentType(oponent.getAgentName(), d.getActions());

			//State s = GridGame.getCorrdinationGameInitialState(d);
			State s = GridGame.getTurkeyInitialState(d);
			//State s = GridGame.getPrisonersDilemmaInitialState(d);

			//System.out.println(s.getCompleteStateDescription());

			JointActionModel jam = new GridGameStandardMechanics(d);
			d.setJointActionModel(jam);

<<<<<<< HEAD
				JointReward jr = new GridGame.GGJointRewardFunction(d, -1, 60.0, 60.0, false);
				TerminalFunction tf = new GridGame.GGTerminalFunction(d);
				SGStateGenerator sg = new ConstantSGStateGenerator(s);
=======
			JointReward jr = new GridGame.GGJointRewardFunction(d, -1, 100.0, 100.0, false);
			TerminalFunction tf = new GridGame.GGTerminalFunction(d);
			SGStateGenerator sg = new ConstantSGStateGenerator(s);
>>>>>>> multi_agent_games

			World gameWorld = new World(d, jr, tf, sg);


			StateHashFactory hashFactory = new DiscreteStateHashFactory();
			BestResponseToDistributionAgent brAgent = new BestResponseToDistributionAgent(d, hashFactory, 100, true);

			if(OTHERFIRST){
				//oponent.joinWorld(gameWorld, oponent.getAgentType());
				oponent.joinWorld(gameWorld, new AgentType(GridGame.CLASSAGENT, d.getObjectClass(GridGame.CLASSAGENT), d.getSingleActions()));
				brAgent.joinWorld(gameWorld, new AgentType(GridGame.CLASSAGENT, d.getObjectClass(GridGame.CLASSAGENT), d.getSingleActions()));
			}else{
				brAgent.joinWorld(gameWorld, new AgentType(GridGame.CLASSAGENT, d.getObjectClass(GridGame.CLASSAGENT), d.getSingleActions()));

				//oponent.joinWorld(gameWorld, oponent.getAgentType());
				oponent.joinWorld(gameWorld, new AgentType(GridGame.CLASSAGENT, d.getObjectClass(GridGame.CLASSAGENT), d.getSingleActions()));
			}

			//construct the other agent policies

			Map<String, Map<Integer,Policy>> allOtherAgentPolicies = new HashMap<String, Map<Integer,Policy>>();
			HashMap<Integer, Policy> levelMap = new HashMap<Integer, Policy>();

			List<SingleAction> actions = d.getSingleActions();

			Policy lowerPolicy;
			if(k==0){
				lowerPolicy = new RandomSingleAgentPolicy(oponent.getAgentName(), actions);

			}else{
				lowerPolicy = previousPolicy;
			}
			HashMap<Integer,Policy> agentPolicies = new HashMap<Integer,Policy>();
			agentPolicies.put(k, lowerPolicy);
			brAgentPolicies.put(oponent.getAgentName(), agentPolicies);

			levelMap.put(k, lowerPolicy);
			String oponentName = oponent.getAgentName();
			allOtherAgentPolicies.put(oponentName, levelMap);


			Map<String, Map<Integer,Double>> distributionOverAllOtherAgentPolicies  = new HashMap<String, Map<Integer,Double>>();
			HashMap<Integer,Double> distribution = new HashMap<Integer,Double>();
			distribution.put(k, 1.0);
			distributionOverAllOtherAgentPolicies.put(oponent.getAgentName(),distribution);

			brAgent.setOtherAgentPolicyMaps(allOtherAgentPolicies, distributionOverAllOtherAgentPolicies);

			//gameWorld.addWorldObserver(ob);
			System.out.println("running game");
			GameAnalysis ga = gameWorld.runGame();
			gas.add(ga);

			System.out.println("Level: "+k+" BR Agent Name: "+brAgent.getAgentName());

			oponent = brAgent;
			previousPolicy = brAgent.policy;
			OTHERFIRST = !OTHERFIRST;

		}
		//OTHERFIRST=false;
		//}

		Visualizer v = GGVisualizer.getVisualizer(6,6);

		GameSequenceVisualizer gsv = new GameSequenceVisualizer(v,d,gas);


		//System.out.println("Reward l1: "+gameWorld.getCumulativeRewardForAgent(brAgent.getAgentName()));
		//System.out.println("Reward l0: "+gameWorld.getCumulativeRewardForAgent(oponent.getAgentName()));
		//System.out.println("game finished");

	}

}
