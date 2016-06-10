package burlap.behavior.singleagent.learnfromdemo.mlirl.differentiableplanners.dpoperator;

import burlap.behavior.functionapproximation.FunctionGradient;
import burlap.behavior.singleagent.planning.stochastic.dpoperator.DPOperator;

/**
 * A {@link DPOperator} that is differentiable.
 * @author James MacGlashan.
 */
public interface DifferentiableDPOperator extends DPOperator {

	/**
	 * Returns the gradient of this DP operator, giving the Q-values on which it operates, their gradient.
	 * @param qs the q-values
	 * @param qGradients the gradients of the Q-value
	 * @return the gradient of this operator
	 */
	FunctionGradient gradient(double [] qs, FunctionGradient[] qGradients);

}
