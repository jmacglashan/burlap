package burlap.behavior.stochasticgame.agents;

import java.util.HashMap;
import java.util.Map;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.ValueFunctionInitialization;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy;
import burlap.behavior.singleagent.planning.stochastic.rtdp.BoundedRTDP;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.stochasticgame.saconversion.MinDistValueFunctionInitialization;
import burlap.behavior.stochasticgame.saconversion.RTDPGreedyQPolicy;
import burlap.behavior.stochasticgame.saconversion.RewardCalculator;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.stochasticgames.Agent;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.SGDomain;

public abstract class BestResponseToDistributionAgent extends Agent {

	protected StateHashFactory hashFactory;
	protected double goalReward;

	protected OOMDPPlanner planner;
	protected Policy policy;
	protected Map<String, Map<Integer, Policy>> allOtherAgentPolicies;
	protected Map<String, Map<Integer, Double>> distributionOverAllOtherAgentPolicies;

	protected boolean runValueIteration;
	protected int numLevels;

	protected boolean isFirstDay;
	protected RewardCalculator rewardCalc;

	protected static double  TAU = 2.0;
	private static double MAX_DIFF = 0.01, GAMMA = 0.99, MAX_DELTA = 0.001;
	private static int MAX_ROLLOUTS = 200, MAX_ROLLOUT_DEPTH = 50,
			MAX_ITERATIONS = 1000000;

	/*
	 * (non-Javadoc)
	 * 
	 * @see burlap.oomdp.stochasticgames.Agent#gameStarting()
	 */
	@Override
	public void gameStarting() {

		// set flag here so when given first state can plan
		isFirstDay = true;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see burlap.oomdp.stochasticgames.Agent#gameTerminated()
	 */
	@Override
	public void gameTerminated() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * burlap.oomdp.stochasticgames.Agent#observeOutcome(burlap.oomdp.core.State
	 * , burlap.oomdp.stochasticgames.JointAction, java.util.Map,
	 * burlap.oomdp.core.State, boolean)
	 */
	@Override
	public void observeOutcome(State s, JointAction jointAction,
			Map<String, Double> jointReward, State sprime, boolean isTerminal) {

		/*
		 * may not need to do anything here because we don't update the agent
		 * while the agent is playing
		 */

	}

	public BestResponseToDistributionAgent(SGDomain domain, StateHashFactory hashFactory,
			double goalReward, boolean runValueIteration, RewardCalculator rewardCalc) {
		this.domain = domain;
		this.hashFactory = hashFactory;
		this.goalReward = goalReward;
		this.runValueIteration = runValueIteration;
		this.rewardCalc = rewardCalc;
		
		this.distributionOverAllOtherAgentPolicies = new HashMap<String, Map<Integer, Double>>();

	}

	/**
	 * Takes as input the map we create and sets the map for this agent
	 * 
	 * @param allOtherAgentPolicies
	 */
	public void setOtherAgentDetails(
			Map<String, Map<Integer, Policy>> allOtherAgentPolicies,
			int numLevels) {

		this.allOtherAgentPolicies = allOtherAgentPolicies;
		this.numLevels = numLevels;
	}

	protected Policy plan(SADomain singleAgentDomain, RewardFunction rf) {
		Policy newPolicy;
		if (runValueIteration) {
			newPolicy = valueIteration(
					"/Users/betsy/research/cognitive_hierarchy/testOut.txt",
					rf, singleAgentDomain, GAMMA, MAX_DELTA, MAX_ITERATIONS);
		} else {
			newPolicy = boundedRTDP(singleAgentDomain, domain,
					this.getAgentName(), rf, GAMMA, goalReward);
		}
		return newPolicy;
	}

	public Policy boundedRTDP(Domain saDomain, Domain ggDomain,
			String agentName, RewardFunction rf, double gamma, double goalReward) {

		ValueFunctionInitialization lowerVInit = new ValueFunctionInitialization.ConstantValueFunctionInitialization(
				-1.0 / (1.0 - gamma));
		ValueFunctionInitialization upperVInit = new MinDistValueFunctionInitialization(
				ggDomain, agentName, goalReward);

		BoundedRTDP brtdp = new BoundedRTDP(saDomain, rf, world.getTF(), gamma,
				hashFactory, lowerVInit, upperVInit, MAX_DIFF, MAX_ROLLOUTS);
		brtdp.setMaxRolloutDepth(MAX_ROLLOUT_DEPTH);
		planner = brtdp;
		planner.planFromState(world.getCurrentWorldState());

		Policy p = new RTDPGreedyQPolicy((QComputablePlanner) planner);

		return p;
	}

	public Policy valueIteration(String outputPath, RewardFunction rf,
			Domain saDomain, double gamma, double maxDelta, int maxIterations) {

		if (!outputPath.endsWith("/")) {
			outputPath = outputPath + "/";
		}

		planner = new ValueIteration(saDomain, rf, world.getTF(), gamma,
				hashFactory, maxDelta, maxIterations);

		planner.planFromState(world.getCurrentWorldState());

		// create a Q-greedy policy from the planner
		Policy p = new GreedyQPolicy((QComputablePlanner) planner);

		return p;
	}

	public Policy getPolicy() {
		return policy;
	}

	public static Map<Integer, Double> getNormalizedDistribution(double tau,
			int maxLevel) {
		HashMap<Integer, Double> distribution = new HashMap<Integer, Double>();
		double facSoFar = 1;
		double weightSum = 0.0;
		for (int lev = 0; lev <= maxLevel; lev++) {
			// the distribution should be based on lev and maxLevel and
			// parameter

			if (lev > 0) {
				facSoFar *= lev;
			}

			double f_k = (Math.pow(Math.E, tau * -1) * Math.pow(tau, lev))
					/ facSoFar;
			weightSum += f_k;
			distribution.put(lev, f_k);
		}

		for (Integer key : distribution.keySet()) {
			// Normalize distribution
			distribution.put(key, distribution.get(key) / weightSum);
			System.out.println("Normalized Dist: " + key + " weight: "
					+ distribution.get(key));
		}

		return distribution;
	}

}
