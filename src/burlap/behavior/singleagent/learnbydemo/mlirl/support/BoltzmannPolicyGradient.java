package burlap.behavior.singleagent.learnbydemo.mlirl.support;

import burlap.behavior.valuefunction.QValue;
import burlap.behavior.singleagent.MDPSolver;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;

import java.util.List;

/**
 * This class provides methods to compute the gradient of a Boltzmann policy. Numerous logarithmic tricks are
 * performed to to avoid overflow issues that a straight computation of the exponentials might induce. The methods
 * require that that input come from a differentiable planner and reward functions, which means that the planner
 * should be implementing a Boltzmann value backup instead of a Bellman value backup.
 * @author James MacGlashan.
 */
public class BoltzmannPolicyGradient {


	/**
	 * Computes the gradient of a Boltzmann policy using the given differentiable planner.
	 * @param s the input state of the policy gradient
	 * @param a the action whose policy probability gradient being queried
	 * @param planner the differentiable {@link burlap.behavior.singleagent.learnbydemo.mlirl.support.QGradientPlanner} planner
	 * @param beta the Boltzmann beta parameter. This parameter is the inverse of the Botlzmann temperature. As beta becomes larger, the policy becomes more deterministic. Should lie in [0, +ifnty].
	 * @return the gradient of the policy.
	 */
	public static double [] computeBoltzmannPolicyGradient(State s, GroundedAction a, QGradientPlanner planner, double beta){

		DifferentiableRF rf = (DifferentiableRF)((MDPSolver)planner).getRF();
		int d = rf.getParameterDimension();

		double gv [] = new double[d];
		for(int i = 0; i < d; i++){
			gv[i] = 0.;
		}

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

		//get all q gradients
		double [][] gqs = new double[qs.length][d];
		for(int i = 0; i < qs.length; i++){
			double [] gq = planner.getQGradient(s, (GroundedAction)Qs.get(i).a).gradient;
			for(int j = 0; j < d; j++){
				gqs[i][j] = gq[j];
			}
		}

		double maxBetaScaled = maxBetaScaled(qs, beta);
		double logSum = logSum(qs, maxBetaScaled, beta);

		double [] policyGradient = computePolicyGradient(rf, beta, qs, maxBetaScaled, logSum, gqs, aind);

		return policyGradient;

	}

	/**
	 * Computes the gradient of a Boltzmann policy using values derived from a Differentiable Botlzmann backup planner.
	 * @param rf the planner's {@link burlap.behavior.singleagent.learnbydemo.mlirl.support.DifferentiableRF}
	 * @param beta the Boltzmann beta parameter. This parameter is the inverse of the Botlzmann temperature. As beta becomes larger, the policy becomes more deterministic. Should lie in [0, +ifnty].
	 * @param qs an array holding the Q-value for each action.
	 * @param maxBetaScaled the maximum Q-value after being scaled by the parameter beta
	 * @param logSum the log sum of the exponentiated q values
	 * @param gqs a matrix holding the Q-value gradient for each action. The matrix's major order is the action index, followed by the parameter gradient
	 * @param aInd the index of the query action for which the policy's gradient is being computed
	 * @return the gradient of the policy.
	 */
	public static double [] computePolicyGradient(DifferentiableRF rf, double beta, double [] qs, double maxBetaScaled, double logSum, double [][] gqs, int aInd){

		int d = rf.getParameterDimension();
		double [] pg = new double[d];

		double constantPart = beta * Math.exp(beta*qs[aInd] + maxBetaScaled - logSum - logSum);

		for(int i = 0; i < qs.length; i++){
			for(int j = 0; j < d; j++){
				pg[j] += (gqs[aInd][j] - gqs[i][j]) * Math.exp(beta * qs[i] - maxBetaScaled);
			}
		}

		for(int j = 0; j < d; j++){
			pg[j] *= constantPart;
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
