package burlap.behavior.singleagent.vfa;

import burlap.oomdp.core.states.State;

/**
 * An interface extension for a {@link burlap.behavior.singleagent.vfa.DifferentiableFunction} that
 * operates on {@link burlap.oomdp.core.states.State} objects. Useful for state value function approximation.
 * @author James MacGlashan.
 */
public interface DifferentiableStateValue extends DifferentiableFunction{

	/**
	 * Sets the input of this function to the given {@link burlap.oomdp.core.states.State} and returns
	 * the value of it. The value may also be retrieved later with the {@link #functionValue()} method.
	 * @param s the {@link burlap.oomdp.core.states.State} to input to the function
	 * @return the value of this function evaluated on the input {@link burlap.oomdp.core.states.State}
	 */
	double functionInput(State s);

}
