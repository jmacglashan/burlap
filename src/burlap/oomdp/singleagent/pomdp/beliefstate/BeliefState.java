package burlap.oomdp.singleagent.pomdp.beliefstate;

import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;

/**
 * An interface for defining a belief state, which is a probability distribution over MDP states. This interface
 * does not require enumerating all states, because it is possible to have a belief state over an infinite number of MDP
 * states. However, it does require that the probability density function be returnable ({@link #belief(State)},
 * to be able to sample an MDP state from the belief distribution {@link #sampleStateFromBelief()},
 * and a mechanism to update the belief state with respect to some observation and action {@link #getUpdatedBeliefState(State, burlap.oomdp.singleagent.GroundedAction)}.
 *
 * @author James MacGlashan and Nakul Gopalan
 */
public interface BeliefState extends State {


	/**
	 * Returns the probability density/mass for the input MDP state.
	 * @param s the the input MDP state defined by a {@link State} instance.
	 * @return the probability density/mass of the input MDP state in this belief distribution.
	 */
	double belief(State s);

	/**
	 * Samples an MDP state state from this belief distribution.
	 * @return an MDP state defined by a {@link State} instance.
	 */
	State sampleStateFromBelief();

	/**
	 * Computes a new belief distribution using this BeliefState as the prior and conditioned on the given POMDP observation
	 * and action taken.
	 * @param observation the conditioned POMDP observation defined by a {@link State} instance.
	 * @param ga the conditioned action selection in the previous time step.
	 * @return the new belief state distribution represented by a new {@link BeliefState} instance.
	 */
	BeliefState getUpdatedBeliefState(State observation, GroundedAction ga);

	
}
