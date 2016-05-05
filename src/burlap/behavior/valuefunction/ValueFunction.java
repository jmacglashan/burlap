package burlap.behavior.valuefunction;

import burlap.mdp.core.state.State;

/**
 * An interface for algorithms that can return the value for states.
 * @author James MacGlashan.
 */
public interface ValueFunction {

	/**
	 * Returns the value function evaluation of the given state. If the value is not stored, then the default value
	 * specified by the ValueFunctionInitialization object of this class is returned.
	 * @param s the state to evaluate.
	 * @return the value function evaluation of the given state.
	 */
	public double value(State s);
}
