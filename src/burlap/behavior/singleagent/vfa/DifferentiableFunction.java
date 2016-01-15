package burlap.behavior.singleagent.vfa;

/**
 * An interface for {@link burlap.behavior.singleagent.vfa.ParametricScalarFunction} implementations
 * that are differentiable.
 * @author James MacGlashan.
 */
public interface DifferentiableFunction extends ParametricScalarFunction {

	/**
	 * Computes the gradient of this function for the last input given.
	 * @return a {@link burlap.behavior.singleagent.vfa.FunctionGradient} specifying this function's gradient.
	 */
	FunctionGradient computeGradient();
}
