package burlap.behavior.functionapproximation;

import burlap.mdp.core.state.State;

/**
 * An extension of {@link ParametricFunction.ParametricStateFunction} that
 * that is differentiable.
 * @author James MacGlashan.
 */
public interface DifferentiableStateValue extends ParametricFunction.ParametricStateFunction{

	/**
	 * Returns the gradient of this function
	 * @param s the input state
	 * @return the gradient
	 */
	FunctionGradient gradient(State s);

}
