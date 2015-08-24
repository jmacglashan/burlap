package burlap.behavior.stochasticgame.saconversion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.Policy.ActionProb;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.stochasticgames.GroundedSingleAction;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.JointActionModel;
import burlap.oomdp.stochasticgames.JointReward;
import burlap.oomdp.stochasticgames.SGDomain;
import burlap.oomdp.stochasticgames.SingleAction;

/**
 * 
 * @author Betsy Hilliard betsy@cs.brown.edu
 *
 */
public class JointRewardFunctionWrapper implements RewardFunction {

	protected JointReward jointReward;
	protected String agentName;
	protected SGDomain sgDomain;
	protected Map<String, Policy> agentPolicyMap;
	protected JointActionModel jam;
	protected List<SingleAction> actions;
	private RewardCalculator rewardCalc;

	public JointRewardFunctionWrapper(JointReward jointReward,
			String agentName, SGDomain sgDomain,
			Map<String, Policy> agentPolicyMap, JointActionModel jam,
			List<SingleAction> actions, RewardCalculator rewardCalc) {
		this.jointReward = jointReward;
		this.agentName = agentName;
		this.sgDomain = sgDomain;
		this.agentPolicyMap = agentPolicyMap;
		this.jam = jam;
		this.actions = actions;
		this.rewardCalc = rewardCalc;
	}

//	public JointRewardFunctionWrapper(JointReward jointReward,
//			String agentName, SGDomain sgDomain,
//			Map<String, Policy> agentPolicyMap, JointActionModel jam,
//			List<SingleAction> actions) {
//		this(jointReward, agentName, sgDomain, agentPolicyMap, jam, actions,
//				new SelfishRewardCalculator());
//	}

	@Override
	public double reward(State s, GroundedAction a, State sprime) {

		// need to get from a GroundedAction to a GroundedSingleAction that I
		// can add to

		// convert GroundedAction to SingleAction

		SingleAction sa = sgDomain.getSingleAction(a.actionName());

		// this is the data structure we will use to calc the expected reward
		List<JointActionProbability> japs = new ArrayList<JointActionProbability>();

		// this map is from agent names to their policy at the given state
		Map<String, List<ActionProb>> mapOfActionProbs = new HashMap<String, List<ActionProb>>();

		// this is used to map a counter to the agent name
		Map<Integer, String> mapping = new HashMap<Integer, String>();
		List<ActionProb> actionProbs = new ArrayList<ActionProb>();
		int i = 0;
		if (agentPolicyMap == null) {
			// create the map from all agents to policy at this state

			// for all actions create uniform
			List<GroundedSingleAction> gsas = sa.getAllGroundedActionsFor(s,
					agentName);

			for (GroundedSingleAction gsa : gsas) {
				actionProbs.add(new ActionProb(gsa, 1.0 / gsas.size()));
			}
			mapOfActionProbs.put(agentName, actionProbs);
			mapping.put(i, agentName);
			i = i + 1;

		} else {

			// create the map from all agents to policy at this state

			for (String agent : agentPolicyMap.keySet()) {
				actionProbs = agentPolicyMap.get(agent)
						.getActionDistributionForState(s);
				mapOfActionProbs.put(agent, actionProbs);
				mapping.put(i, agent);
				i = i + 1;
			}
		}

		// add the non-normalized probability of taking all next joint actions
		japs = addAllCombinations(s, mapOfActionProbs, mapping, a.params, sa);
		double expectedReward = 0.0;

		// sum over all next states so me can normalize
		double reward = 0.0;
		for (JointActionProbability jap : japs) {
			List<TransitionProbability> transProbs = jam.transitionProbsFor(s,
					jap.getJointAction());
			for (TransitionProbability tp : transProbs) {
				double otherReward = 0;
				double myReward = 0;
				myReward = jointReward.reward(s, jap.getJointAction(), sprime)
						.get(agentName);

				if (agentName.contains("0")) {
					otherReward = jointReward.reward(s, jap.getJointAction(),
							sprime).get("agent1");
				} else {
					otherReward = jointReward.reward(s, jap.getJointAction(),
							sprime).get("agent0");
				}

				reward = this.rewardCalc.getReward(myReward, otherReward);
				expectedReward += reward * jap.getProbability() * tp.p;

			}

		}

		return expectedReward;
	}

	private List<JointActionProbability> addAllCombinations(State s,
			Map<String, List<ActionProb>> lists, Map<Integer, String> mapping,
			String[] params, SingleAction sa) {

		// this array just tracks where we are in each list of actions for each
		// other agent
		int[] counters = new int[lists.size()];

		//
		List<JointActionProbability> jaProbs = new ArrayList<JointActionProbability>();
		do {

			JointActionProbability jaProb = getJointActionProbabilities(s,
					counters, mapping, lists, params, sa);
			jaProbs.add(jaProb);

		} while (increment(counters, mapping, lists));

		return jaProbs;
	}

	/**
	 * 
	 * @param state
	 * @param counters
	 * @param mapping
	 * @param sets
	 * @param params
	 * @return
	 */
	private JointActionProbability getJointActionProbabilities(State state,
			int[] counters, Map<Integer, String> mapping,
			Map<String, List<ActionProb>> sets, String[] params, SingleAction sa) {

		// we are going to create a fake joint action for a set of other agent
		// actions
		JointAction ja = new JointAction();

		// add the action agent's action
		ja.addAction(new GroundedSingleAction(agentName, sa, params));

		// create and calculate the probability of this joint action
		double probOfJA = 1.0;
		for (int i = 0; i < counters.length; i++) {
			String otherAgentName = mapping.get(i);
			int actionNum = counters[i];
			// Add to the joint action the grounded version of the acting
			// agent's corresponding SingleAction and parameters:

			AbstractGroundedAction aga = sets.get(otherAgentName)
					.get(actionNum).ga; // this is a GroundedAction
			// temp.actionName();
			// saDomain.getAction(temp.actionName());

			Random rand = new Random();
			List<GroundedSingleAction> otherAgentsActions = new ArrayList<GroundedSingleAction>();
			for (SingleAction posSA : actions) {
				if (posSA.actionName == aga.actionName()) {
					otherAgentsActions = posSA.getAllGroundedActionsFor(state,
							otherAgentName);
				}
			}

			ja.addAction(otherAgentsActions.get(rand.nextInt(otherAgentsActions
					.size())));

			// multiply in the prob of this action from the other agents'
			// policies
			probOfJA = probOfJA
					* agentPolicyMap.get(otherAgentName).getProbOfAction(state,
							sets.get(otherAgentName).get(actionNum).ga);
		}

		JointActionProbability jap = new JointActionProbability(ja, probOfJA);
		// return the next states
		return jap;
	}

	/**
	 * this increments the counters so that we go through all other agent
	 * actions
	 * 
	 * @param counters
	 * @param mapping
	 * @param sets
	 * @return
	 */
	private static boolean increment(int[] counters,
			Map<Integer, String> mapping, Map<String, List<ActionProb>> sets) {
		for (int i = counters.length - 1; i >= 0; i--) {
			if (counters[i] < sets.get(mapping.get(i)).size() - 1) {
				counters[i]++;
				return true;
			} else {
				counters[i] = 0;
			}
		}
		return false;
	}

}
