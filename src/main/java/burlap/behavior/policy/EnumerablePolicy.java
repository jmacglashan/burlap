package burlap.behavior.policy;

import burlap.behavior.policy.support.ActionProb;
import burlap.mdp.core.state.State;

import java.util.List;

/**
 * An interface extension to {@link Policy} for policies that can enumerate their probability distribution over all actions.
 * @author James MacGlashan.
 */
public interface EnumerablePolicy extends Policy {

	/**
	 * This method will return action probability distribution defined by the policy. The action distribution is represented
	 * by a list of ActionProb objects, each which specifies a grounded action and a probability of that grounded action being
	 * taken. The returned list does not have to include actions with probability 0.
	 * @param s the state for which an action distribution should be returned
	 * @return a list of possible actions taken by the policy and their probability.
	 */
	List<ActionProb> policyDistribution(State s);


}
