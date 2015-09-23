package burlap.behavior.stochasticgames.agents;

import java.util.HashMap;
import java.util.Map;

import burlap.behavior.policy.Policy;
import burlap.behavior.stochasticgames.saconversion.ConversionGenerator;
import burlap.behavior.stochasticgames.saconversion.ExpectedPolicyWrapper;
import burlap.behavior.stochasticgames.saconversion.JointRewardFunctionWrapper;
import burlap.behavior.stochasticgames.saconversion.RewardCalculator;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.statehashing.HashableStateFactory;
import burlap.oomdp.stochasticgames.SGDomain;
import burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction;
import burlap.oomdp.stochasticgames.agentactions.ObParamSGAgentAction.GroundedObParamSGAgentAction;

public class BRDPlanThenCombinePoliciesAgent extends
		BestResponseToDistributionAgent {

	public BRDPlanThenCombinePoliciesAgent(SGDomain domain, HashableStateFactory hashFactory,
			double goalReward, boolean runValueIteration, RewardCalculator rewardCalc) {
		super(domain, hashFactory, goalReward, runValueIteration, rewardCalc);
	}

	@Override
	public GroundedSGAgentAction getAction(State s) {
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
						domain.getAgentActions(), this.rewardCalc);

				Policy newPolicy = plan(singleAgentDomain, rf);

				newPolicies.put(lev, newPolicy);
				//policy = newPolicy;
			}
			policy = constructPolicy(newPolicies, otherAgentName);
			
			// reset isFirstDay
			isFirstDay = false;
		}
		// change single action to multi agent action
		// return from policy generated at beginning

		// GroundedAction to GroundedSingleAction
		AbstractGroundedAction ga = policy.getAction(s);

		GroundedSGAgentAction gsa = new GroundedObParamSGAgentAction(getAgentName(),
				domain.getSingleAction(ga.actionName()), ga.getParametersAsString());

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
