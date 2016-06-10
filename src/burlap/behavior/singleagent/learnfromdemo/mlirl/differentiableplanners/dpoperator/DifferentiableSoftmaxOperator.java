package burlap.behavior.singleagent.learnfromdemo.mlirl.differentiableplanners.dpoperator;

import burlap.behavior.functionapproximation.FunctionGradient;
import burlap.behavior.singleagent.learnfromdemo.mlirl.support.BoltzmannPolicyGradient;
import burlap.behavior.singleagent.planning.stochastic.dpoperator.SoftmaxOperator;

/**
 * @author James MacGlashan.
 */
public class DifferentiableSoftmaxOperator extends SoftmaxOperator implements DifferentiableDPOperator{

	public DifferentiableSoftmaxOperator() {
		super();
	}

	public DifferentiableSoftmaxOperator(double beta) {
		super(beta);
	}

	@Override
	public FunctionGradient gradient(double [] qs, FunctionGradient[] qGradients) {
		FunctionGradient vGradient = new FunctionGradient.SparseGradient();

		double maxBetaScaled = BoltzmannPolicyGradient.maxBetaScaled(qs, this.beta);
		double logSum = BoltzmannPolicyGradient.logSum(qs, maxBetaScaled, this.beta);

		for(int i = 0; i < qs.length; i++){

			double probA = Math.exp(this.beta * qs[i] - logSum);
			FunctionGradient policyGradient = BoltzmannPolicyGradient.computePolicyGradient(this.beta, qs, maxBetaScaled, logSum, qGradients, i);

			for(FunctionGradient.PartialDerivative pd : policyGradient.getNonZeroPartialDerivatives()){
				double curVal = vGradient.getPartialDerivative(pd.parameterId);
				double nextVal = curVal + (probA * qGradients[i].getPartialDerivative(pd.parameterId)) + qs[i] * pd.value;
				vGradient.put(pd.parameterId, nextVal);
			}


		}
		return vGradient;
	}

}
