package burlap.behavior.singleagent.learnfromdemo.mlirl.support;

import burlap.behavior.singleagent.vfa.FunctionGradient;
import burlap.behavior.valuefunction.QValue;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;

import java.util.List;
import java.util.Map;

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

		FunctionGradient policyGradient = computePolicyGradient(beta, qs, maxBetaScaled, logSum, qGradients, aind);

		return policyGradient;

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

		FunctionGradient pg = new FunctionGradient();
		double constantPart = beta * Math.exp(beta*qs[aInd] + maxBetaScaled - logSum - logSum);
		for(int i = 0; i < qs.length; i++){
			for(Map.Entry<Integer, Double> pd : gqs[i].getNonZeroPartialDerivatives()){
				double curVal = pg.getPartialDerivative(pd.getKey());
				double nextVal = curVal + (gqs[aInd].getPartialDerivative(pd.getKey()) - gqs[i].getPartialDerivative(pd.getKey()))
											* Math.exp(beta * qs[i] - maxBetaScaled);

				pg.put(pd.getKey(), nextVal);
 			}
		}

		for(Map.Entry<Integer, Double> pd : pg.getNonZeroPartialDerivatives()){
			double nextVal = pd.getValue() * constantPart;
			pg.put(pd.getKey(), nextVal);
		}

		return pg;

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
		double v = maxBetaScaled + Math.log(expSum);
		return v;

	}


}
