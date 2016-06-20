package burlap.behavior.valuefunction;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;

/**
 * Interface for objects that define a Q-function
 * @author James MacGlashan.
 */
public interface QFunction extends ValueFunction{
	/**
	 * Returns the {@link burlap.behavior.valuefunction.QValue} for the given state-action pair.
	 * @param s the input state
	 * @param a the input action
	 * @return the {@link burlap.behavior.valuefunction.QValue} for the given state-action pair.
	 */
	double qValue(State s, Action a);
}
