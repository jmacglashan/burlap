package burlap.behavior.singleagent.learnfromdemo.mlirl.differentiableplanners.dpoperator;

import burlap.behavior.functionapproximation.FunctionGradient;
import burlap.behavior.singleagent.planning.stochastic.dpoperator.DPOperator;

/**
 * @author James MacGlashan.
 */
public interface DifferentiableDPOperator extends DPOperator {

	FunctionGradient gradient(double [] qs, FunctionGradient[] qGradients);

}
