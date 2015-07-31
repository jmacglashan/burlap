/**
 * 
 */
package burlap.behavior.stochasticgame.agents;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.stochasticgame.saconversion.ConversionGenerator;
import burlap.behavior.stochasticgame.saconversion.ExpectedPolicyWrapper;
import burlap.behavior.stochasticgame.saconversion.JointRewardFunctionWrapper;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.common.CachedRewardFunction;
import burlap.oomdp.stochasticgames.GroundedSingleAction;
import burlap.oomdp.stochasticgames.SGDomain;

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

	public BRDCombinePoliciesThenPlanAgent(SGDomain domain, StateHashFactory hashFactory,
			double goalReward, boolean runValueIteration) {
		super(domain, hashFactory, goalReward, runValueIteration);
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
	public GroundedSingleAction getAction(State s) {
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
					new DiscreteStateHashFactory(),
					new JointRewardFunctionWrapper(world.getRewardModel(),
							getAgentName(), domain, otherAgentPolicies,
							world.getActionModel(), domain.getSingleActions()));

			policy = plan(singleAgentDomain, rf);
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
}
