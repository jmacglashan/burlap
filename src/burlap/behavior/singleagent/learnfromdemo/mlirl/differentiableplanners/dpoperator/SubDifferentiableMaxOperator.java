package burlap.behavior.singleagent.learnfromdemo.mlirl.differentiableplanners.dpoperator;

import burlap.behavior.functionapproximation.FunctionGradient;
import burlap.behavior.singleagent.planning.stochastic.dpoperator.BellmanOperator;

import java.util.Set;

/**
 * Provides the sub gradient of the {@link BellmanOperator} max operator.
 * @author James MacGlashan.
 */
public class SubDifferentiableMaxOperator extends BellmanOperator implements DifferentiableDPOperator {

	@Override
	public FunctionGradient gradient(double[] qs, FunctionGradient[] qGradients) {

		double mx = qs[0];
		int mxi = 0;
		for(int i = 0; i < qs.length; i++){
			double qi = qs[i];
			if(qi > mx){
				mx = qi;
				mxi = i;
			}
		}

		Set<FunctionGradient.PartialDerivative> pds = qGradients[mxi].getNonZeroPartialDerivatives();
		FunctionGradient gradient = new FunctionGradient.SparseGradient(pds.size());
		for(FunctionGradient.PartialDerivative pd : pds){
			gradient.put(pd.parameterId, pd.value);
		}

		return gradient;
	}
}
