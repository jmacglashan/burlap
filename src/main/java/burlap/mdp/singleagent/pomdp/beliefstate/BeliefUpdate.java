package burlap.mdp.singleagent.pomdp.beliefstate;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;

/**
 * An interface for defining methods that can compute an updated belief state given a prior belief state, a new
 * observation, and the action that led to that observation.
 * @author James MacGlashan.
 */
public interface BeliefUpdate {
	/**
	 * Computes a new belief distribution from a previous belief and given a new observation received after taking
	 * a specific action.
	 * @param belief the prior belief
	 * @param observation the conditioned POMDP observation defined by a {@link State} instance.
	 * @param a the conditioned action selection in the previous time step.
	 * @return the new belief state distribution represented by a new {@link BeliefState} instance.
	 */
	BeliefState update(BeliefState belief, State observation, Action a);
}
