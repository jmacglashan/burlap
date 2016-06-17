package burlap.behavior.functionapproximation;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;

/**
 * An extension of {@link ParametricFunction.ParametricStateActionFunction} that
 * is differentiable.
 * Useful for state-action value function approximation (e.g., Q-value function approximation).
 * @author James MacGlashan.
 */
public interface DifferentiableStateActionValue extends ParametricFunction.ParametricStateActionFunction {

	/**
	 * Returns the gradient of this function.
	 * @param s the input {@link State}
	 * @param a the input {@link Action}
	 * @return the {@link FunctionGradient} of this function at the input
	 */
	FunctionGradient gradient(State s, Action a);

}
