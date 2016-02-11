package burlap.behavior.singleagent.vfa;

import burlap.oomdp.core.states.State;

/**
 * An extension of {@link burlap.behavior.singleagent.vfa.ParametricScalarFunction.ParametricStateFunction} that
 * that is differentiable.
 * @author James MacGlashan.
 */
public interface DifferentiableStateValue extends ParametricScalarFunction.ParametricStateFunction{

	/**
	 * Returns the gradient of this function
	 * @param s the input state
	 * @return the gradient
	 */
	FunctionGradient gradient(State s);

}
