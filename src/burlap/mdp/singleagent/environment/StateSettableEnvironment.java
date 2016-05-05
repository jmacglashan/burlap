package burlap.mdp.singleagent.environment;

import burlap.mdp.core.state.State;

/**
 * An interface to be used with {@link burlap.mdp.singleagent.environment.Environment} instances that allows
 * the environment to have its set set to a client specified state.
 * @author James MacGlashan.
 */
public interface StateSettableEnvironment extends Environment{

	/**
	 * Sets the current state of the environment to the specified state.
	 * @param s the state to which this {@link burlap.mdp.singleagent.environment.Environment} will be set.
	 */
	void setCurStateTo(State s);
}
