package burlap.behavior.singleagent.vfa;

import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.states.State;

/**
 * An interface extension for a {@link burlap.behavior.singleagent.vfa.DifferentiableFunction} that
 * operates on {@link burlap.oomdp.core.states.State} and {@link burlap.oomdp.core.AbstractGroundedAction} objects.
 * Useful for state-action value function approximation (e.g., Q-value function approximation).
 * @author James MacGlashan.
 */
public interface DifferentiableStateActionValue extends DifferentiableFunction {

	/**
	 * Sets the input of this function to the given {@link burlap.oomdp.core.states.State} and
	 * {@link burlap.oomdp.core.AbstractGroundedAction}, and then returns
	 * the value of it. The value may also be retrieved later with the {@link #functionValue()} method.
	 * @param s the {@link burlap.oomdp.core.states.State} to input to the function.
	 * @param a the {@link burlap.oomdp.core.AbstractGroundedAction} to input to the function.
	 * @return 	 * @return the value of this function evaluated on the input {@link burlap.oomdp.core.states.State} and {@link burlap.oomdp.core.AbstractGroundedAction}.

	 */
	double functionInput(State s, AbstractGroundedAction a);

}
