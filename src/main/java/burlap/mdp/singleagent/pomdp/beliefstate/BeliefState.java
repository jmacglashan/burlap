package burlap.mdp.singleagent.pomdp.beliefstate;

import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;

/**
 * An interface for defining a belief state, which is a probability distribution over states. This interface
 * does not require enumerating all states, because it is possible to have a belief state over an infinite number of MDP
 * states. However, it does require that the probability density function for a specified state be returnable ({@link #belief(State)},
 * to be able to sample an MDP state from the belief distribution with {@link #sample()},
 * and a mechanism to update the belief state with respect to some observation and action with {@link #update(State, Action)}.
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
	State sample();

	
}
