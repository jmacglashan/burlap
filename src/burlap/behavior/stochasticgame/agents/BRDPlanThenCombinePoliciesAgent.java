package burlap.behavior.stochasticgame.agents;

import java.util.HashMap;
import java.util.Map;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.stochasticgame.saconversion.ConversionGenerator;
import burlap.behavior.stochasticgame.saconversion.ExpectedPolicyWrapper;
import burlap.behavior.stochasticgame.saconversion.JointRewardFunctionWrapper;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.stochasticgames.GroundedSingleAction;
import burlap.oomdp.stochasticgames.SGDomain;

public class BRDPlanThenCombinePoliciesAgent extends
		BestResponseToDistributionAgent {

	public BRDPlanThenCombinePoliciesAgent(SGDomain domain, StateHashFactory hashFactory,
			double goalReward, boolean runValueIteration) {
		super(domain, hashFactory, goalReward, runValueIteration);
	}

	@Override
	public GroundedSingleAction getAction(State s) {
		if (isFirstDay) {

			for (String otherName : allOtherAgentPolicies.keySet()) {
				distributionOverAllOtherAgentPolicies.put(otherName,
						getNormalizedDistribution(TAU, numLevels));
			}

			Map<Integer, Policy> newPolicies = new HashMap<Integer, Policy>();

			String otherAgentName = null;
			for (String name : allOtherAgentPolicies.keySet()) {
				if (name.compareToIgnoreCase(getAgentName()) != 0) {
					otherAgentName = name;
				}
			}

			SADomain singleAgentDomain;

			// plan and create policy
			for (int lev = 0; lev <= numLevels; lev++) {

				Map<String, Policy> levelPolicies = getAgentPoliciesForLevel(lev);

				ConversionGenerator generator = new ConversionGenerator(domain,
						world.getActionModel(), agentType, worldAgentName,
						levelPolicies, hashFactory);

				singleAgentDomain = (SADomain) generator.generateDomain();

				RewardFunction rf = new JointRewardFunctionWrapper(
						world.getRewardModel(), getAgentName(), domain,
						levelPolicies, world.getActionModel(),
						domain.getSingleActions());

				Policy newPolicy = plan(singleAgentDomain, rf);

				newPolicies.put(lev, newPolicy);
			}
			policy = constructPolicy(newPolicies, otherAgentName);

			// reset isFirstDay
			isFirstDay = false;
		}
		// change single action to multi agent action
		// return from policy generated at beginning

		// GroundedAction to GroundedSingleAction
		AbstractGroundedAction ga = policy.getAction(s);

		GroundedSingleAction gsa = new GroundedSingleAction(getAgentName(),
				domain.getSingleAction(ga.actionName()), ga.params);

		return gsa;

	}

	/**
	 * Gets the policy for a given level
	 * 
	 */
	private Map<String, Policy> getAgentPoliciesForLevel(int lev) {

		Map<String, Policy> policyMap = new HashMap<String, Policy>();
		for (String otherAgentName : allOtherAgentPolicies.keySet()) {

			policyMap.put(otherAgentName,
					allOtherAgentPolicies.get(otherAgentName).get(lev));
		}
		return policyMap;
	}

	/**
	 * Constructs a policy from set of policies that were learned, one for each
	 * level.
	 * 
	 * @param newPolicies
	 * @param otherAgentName
	 * @return
	 */
	private Policy constructPolicy(Map<Integer, Policy> newPolicies,
			String otherAgentName) {

		Policy newPolicy = new ExpectedPolicyWrapper(newPolicies,
				distributionOverAllOtherAgentPolicies.get(otherAgentName));

		return newPolicy;
	}
}
