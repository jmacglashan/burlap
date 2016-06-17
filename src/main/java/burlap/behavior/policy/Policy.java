package burlap.behavior.policy;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;


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
	 * Returns the probability/probability density that the given action will be taken in the given state.
	 * @param s the state of interest
	 * @param a the action that may be taken in the state
	 * @return the probability/probability density
	 */
	double actionProb(State s, Action a);

	/**
	 * Specifies whether this policy is defined for the input state.
	 * @param s the input state to test for whether this policy is defined
	 * @return true if this policy is defined for {@link State} s, false otherwise.
	 */
	boolean definedFor(State s);


}
