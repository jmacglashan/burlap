package burlap.mdp.singleagent.model;

import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;

/**
 * Interface for model that can be used to sample a transition from an input state for a given action and can indicate
 * when a state is terminal or not.
 * @author James MacGlashan.
 */
public interface SampleModel {

	/**
	 * Samples a transition from the transition distribution and returns it.
	 * @param s the source state
	 * @param a the action taken in the source state
	 * @return and {@link EnvironmentOutcome} describing the sampled transition
	 */
	EnvironmentOutcome sample(State s, Action a);

	/**
	 * Indicates whether a state is a terminal state (i.e., no more action occurs and zero reward received from there on out)
	 * @param s the input state to test
	 * @return true if the state is a terminal state, false if it is not.
	 */
	boolean terminal(State s);
}
