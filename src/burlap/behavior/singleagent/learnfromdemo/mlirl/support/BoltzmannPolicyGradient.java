package burlap.behavior.singleagent.learnfromdemo.mlirl.support;

import burlap.behavior.singleagent.vfa.FunctionGradient;
import burlap.behavior.valuefunction.QValue;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class provides methods to compute the gradient of a Boltzmann policy. Numerous logarithmic tricks are
 * performed to to avoid overflow issues that a straight computation of the exponentials might induce. The methods
 * require that that input come from a differentiable valueFunction and reward functions, which means that the valueFunction
 * should be implementing a Boltzmann value backup instead of a Bellman value backup.
 * @author James MacGlashan.
 */
public class BoltzmannPolicyGradient {


	/**
	 * Computes the gradient of a Boltzmann policy using the given differentiable valueFunction.
	 * @param s the input state of the policy gradient
	 * @param a the action whose policy probability gradient being queried
	 * @param planner the differentiable {@link burlap.behavior.singleagent.learnfromdemo.mlirl.support.QGradientPlanner} valueFunction
	 * @param beta the Boltzmann beta parameter. This parameter is the inverse of the Botlzmann temperature. As beta becomes larger, the policy becomes more deterministic. Should lie in [0, +ifnty].
	 * @return the gradient of the policy.
	 */
	public static FunctionGradient computeBoltzmannPolicyGradient(State s, GroundedAction a, QGradientPlanner planner, double beta){


		//get q objects
		List<QValue> Qs = planner.getQs(s);
		double [] qs = new double[Qs.size()];
		for(int i = 0; i < Qs.size(); i++){
			qs[i] = Qs.get(i).q;
		}

		//find matching action index
		int aind = -1;
		for(int i = 0; i < Qs.size(); i++){
			if(Qs.get(i).a.equals(a)){
				aind = i;
				break;
			}
		}

		if(aind == -1){
			throw new RuntimeException("Error in computing BoltzmannPolicyGradient: Could not find query action in Q-value list.");
		}

		FunctionGradient [] qGradients = new FunctionGradient[qs.length];
		for(int i = 0; i < qs.length; i++){
			qGradients[i] = planner.getQGradient(s, (GroundedAction)Qs.get(i).a).gradient;
		}


		double maxBetaScaled = maxBetaScaled(qs, beta);
		double logSum = logSum(qs, maxBetaScaled, beta);

		return computePolicyGradient(beta, qs, maxBetaScaled, logSum, qGradients, aind);

	}

	/**
	 * Computes the gradient of a Boltzmann policy using values derived from a Differentiable Botlzmann backup valueFunction.
	 * @param beta the Boltzmann beta parameter. This parameter is the inverse of the Botlzmann temperature. As beta becomes larger, the policy becomes more deterministic. Should lie in [0, +ifnty].
	 * @param qs an array holding the Q-value for each action.
	 * @param maxBetaScaled the maximum Q-value after being scaled by the parameter beta
	 * @param logSum the log sum of the exponentiated q values
	 * @param gqs a matrix holding the Q-value gradient for each action. The matrix's major order is the action index, followed by the parameter gradient
	 * @param aInd the index of the query action for which the policy's gradient is being computed
	 * @return the gradient of the policy.
	 */
	public static FunctionGradient computePolicyGradient(double beta, double [] qs, double maxBetaScaled, double logSum, FunctionGradient [] gqs, int aInd){

		FunctionGradient pg = new FunctionGradient.SparseGradient();
		double constantPart = beta * Math.exp(beta*qs[aInd] + maxBetaScaled - logSum - logSum);
		Set<Integer> nzPDs = combinedNonZeroPDParameters(gqs);
		for(int i = 0; i < qs.length; i++){
			for(int param : nzPDs){
				double curVal = pg.getPartialDerivative(param);
				double nextVal = curVal + (gqs[aInd].getPartialDerivative(param) - gqs[i].getPartialDerivative(param))
											* Math.exp(beta * qs[i] - maxBetaScaled);

				pg.put(param, nextVal);
 			}
		}

		FunctionGradient finalGradient = new FunctionGradient.SparseGradient(pg.numNonZeroPDs());
		for(FunctionGradient.PartialDerivative pd : pg.getNonZeroPartialDerivatives()){
			double nextVal = pd.value * constantPart;
			finalGradient.put(pd.parameterId, nextVal);
		}

		return finalGradient;

	}


	/**
	 * Given an array of Q-values, returns the maximum Q-value multiplied by the parameter beta.
	 * @param qs an array of Q-values
	 * @param beta the scaling beta parameter.
	 * @return the maximum Q-value multiplied by the parameter beta
	 */
	public static double maxBetaScaled(double [] qs, double beta){
		double max = Double.NEGATIVE_INFINITY;
		for(double q : qs){
			if(q > max){
				max = q;
			}
		}
		return beta*max;
	}


	/**
	 * Computes the log sum of exponentiated Q-values (Scaled by beta)
	 * @param qs the Q-values
	 * @param maxBetaScaled the maximum Q-value scaled by the parameter beta
	 * @param beta the scaling value.
	 * @return the log sum of exponentiated Q-values (Scaled by beta)
	 */
	public static double logSum(double [] qs, double maxBetaScaled, double beta){

		double expSum = 0.;
		for(int i = 0; i < qs.length; i++){
			expSum += Math.exp(beta * qs[i] - maxBetaScaled);
		}
		return maxBetaScaled + Math.log(expSum);
	}

	protected static Set<Integer> combinedNonZeroPDParameters(FunctionGradient...gradients){

		Set<Integer> c = new HashSet<Integer>();
		for(FunctionGradient g : gradients){
			Set<FunctionGradient.PartialDerivative> p = g.getNonZeroPartialDerivatives();
			for(FunctionGradient.PartialDerivative e : p){
				c.add(e.parameterId);
			}
		}

		return c;
	}


}
