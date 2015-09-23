/**
 * 
 */
package burlap.behavior.stochasticgames.agents;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import burlap.behavior.policy.Policy;
import burlap.behavior.stochasticgames.saconversion.ConversionGenerator;
import burlap.behavior.stochasticgames.saconversion.ExpectedPolicyWrapper;
import burlap.behavior.stochasticgames.saconversion.JointRewardFunctionWrapper;
import burlap.behavior.stochasticgames.saconversion.RewardCalculator;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.common.CachedRewardFunction;
import burlap.oomdp.statehashing.HashableStateFactory;
import burlap.oomdp.statehashing.SimpleHashableStateFactory;
import burlap.oomdp.stochasticgames.SGDomain;
import burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction;
import burlap.oomdp.stochasticgames.agentactions.ObParamSGAgentAction.GroundedObParamSGAgentAction;

/**
 * 
 * This agent takes in a set of opponent agent policies and a distribution over
 * those opponent agents and returns a best response policy that assumes a
 * different opponent is chosen, from the given distribution, at each step.
 * 
 * This agent assumes there is a cognitive hierarchy of agents it is playing
 * against. For more information, see [Wunder, Littman and Stone, 2009]
 * 
 * @author Betsy Hilliard (betsy@cs.brown.edu)
 *
 */
public class BRDCombinePoliciesThenPlanAgent extends
		BestResponseToDistributionAgent {

	public BRDCombinePoliciesThenPlanAgent(SGDomain domain, HashableStateFactory hashFactory,
			double goalReward, boolean runValueIteration, RewardCalculator rewardCalc) {
		super(domain, hashFactory, goalReward, runValueIteration, rewardCalc);
	}

	/**
	 * This picks an agent based on the distribution
	 * 
	 */
	private Map<String, Policy> chooseOtherAgentPolicies() {
		Random rand = new Random();

		Map<String, Policy> policyMap = new HashMap<String, Policy>();
		for (String otherAgentName : allOtherAgentPolicies.keySet()) {
			double draw = rand.nextDouble();
			double total = 0.0;
			Integer levelChosen = null;
			for (Integer level : distributionOverAllOtherAgentPolicies.get(
					otherAgentName).keySet()) {
				if (distributionOverAllOtherAgentPolicies.get(otherAgentName)
						.get(level) + total > draw
						&& levelChosen == null) {
					levelChosen = level;
				} else {
					total = total
							+ distributionOverAllOtherAgentPolicies.get(
									otherAgentName).get(level);
				}
			}

			policyMap.put(otherAgentName,
					allOtherAgentPolicies.get(otherAgentName).get(levelChosen));
		}
		return policyMap;
	}

	/**
	 * this combines the policies into one per other agent based on the
	 * distribution
	 * 
	 */
	private Map<String, Policy> constructOtherAgentPolicies() {

		Map<String, Policy> policyMap = new HashMap<String, Policy>();
		for (String otherAgentName : allOtherAgentPolicies.keySet()) {
			Policy newPolicy = new ExpectedPolicyWrapper(
					allOtherAgentPolicies.get(otherAgentName),
					distributionOverAllOtherAgentPolicies.get(otherAgentName));

			policyMap.put(otherAgentName, newPolicy);
		}
		return policyMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * burlap.oomdp.stochasticgames.Agent#getAction(burlap.oomdp.core.State)
	 */
	@Override
	public GroundedSGAgentAction getAction(State s) {
		SADomain singleAgentDomain;

		if (isFirstDay) {
			// plan and create policy

			for (String otherName : allOtherAgentPolicies.keySet()) {
				distributionOverAllOtherAgentPolicies.put(otherName,
						getNormalizedDistribution(TAU, numLevels));
			}

			Map<String, Policy> otherAgentPolicies = constructOtherAgentPolicies();

			ConversionGenerator generator = new ConversionGenerator(domain,
					world.getActionModel(), agentType, worldAgentName,
					otherAgentPolicies, hashFactory);

			singleAgentDomain = (SADomain) generator.generateDomain();

			CachedRewardFunction rf = new CachedRewardFunction(
					new SimpleHashableStateFactory(),
					new JointRewardFunctionWrapper(world.getRewardModel(),
							getAgentName(), domain, otherAgentPolicies,
							world.getActionModel(), domain.getAgentActions(), this.rewardCalc));

			policy = plan(singleAgentDomain, rf);
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
}
