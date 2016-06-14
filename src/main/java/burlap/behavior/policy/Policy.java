package burlap.behavior.policy;

import burlap.behavior.policy.support.ActionProb;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;

import java.util.List;


/**
 * An interface for defining a {@link Policy}. Requires providing an action mapping (or sampling in the case
 * of stochastic policies) specifying the policy distribution, indicating whether the policy is stochastic,
 * and checking if the policy is defined for an input state.
 * <p>
 * Various helper methods, including methods to rollout a policy from a model or in an environment are
 * included in the {@link PolicyUtils} class.
 *
 *
 * @author James MacGlashan
 *
 */
public interface Policy {


	
	/**
	 * This method will return an action sampled by the policy for the given state. If the defined policy is
	 * stochastic, then multiple calls to this method for the same state may return different actions. The sampling
	 * should be with respect to defined action distribution that is returned by getActionDistributionForState
	 * @param s the state for which an action should be returned
	 * @return a sample action from the action distribution; null if the policy is undefined for s
	 */
	Action action(State s);
	
	/**
	 * This method will return action probability distribution defined by the policy. The action distribution is represented
	 * by a list of ActionProb objects, each which specifies a grounded action and a probability of that grounded action being
	 * taken. The returned list does not have to include actions with probability 0.
	 * @param s the state for which an action distribution should be returned
	 * @return a list of possible actions taken by the policy and their probability. 
	 */
	List<ActionProb> policyDistribution(State s); //returns null when policy is undefined for s
	

	/**
	 * Specifies whether this policy is defined for the input state.
	 * @param s the input state to test for whether this policy is defined
	 * @return true if this policy is defined for {@link State} s, false otherwise.
	 */
	boolean definedFor(State s);


}
