package burlap.behavior.singleagent.learnfromdemo.mlirl.support;

import burlap.behavior.functionapproximation.FunctionGradient;
import burlap.behavior.functionapproximation.GradientUtils;
import burlap.behavior.valuefunction.QProvider;
import burlap.behavior.valuefunction.QValue;
import burlap.datastructures.BoltzmannDistribution;
import burlap.datastructures.HashedAggregator;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;

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

    private BoltzmannPolicyGradient() {
        // do nothing
    }

	/**
	 * Computes the gradient of a Boltzmann policy using the given differentiable valueFunction.
	 * @param s the input state of the policy gradient
	 * @param a the action whose policy probability gradient being queried
	 * @param planner the differentiable {@link DifferentiableQFunction} valueFunction
	 * @param beta the Boltzmann beta parameter. This parameter is the inverse of the Botlzmann temperature. As beta becomes larger, the policy becomes more deterministic. Should lie in [0, +ifnty].
	 * @return the gradient of the policy.
	 */
	public static FunctionGradient computeBoltzmannPolicyGradient(State s, Action a, DifferentiableQFunction planner, double beta){


		//get q objects
		List<QValue> Qs = ((QProvider)planner).qValues(s);
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
			qGradients[i] = planner.qGradient(s, Qs.get(i).a);
		}


		FunctionGradient policyGradient = computePolicyGradient(qs, qGradients, aind, beta);

		return policyGradient;

	}

    /**
     * Computes the gradient of the Boltzmann (softmax) policy wrt some parameters.
     * @param prefs the action-wise preference values passed through the softmax
     * @param grads the gradients of the preference-values with respect to the parameters
     * @param aind the index of the action for which the gradient is being queried
     * @param beta the softmax beta parameter. This parameter is the inverse of the Botlzmann temperature. As beta becomes larger, the policy becomes more deterministic. Should lie in [0, +ifnty].
     * @return the gradient of the policy
     */
	public static FunctionGradient computePolicyGradient(double [] prefs, FunctionGradient[] grads, int aind, double beta){

		//first compute policy probs
		BoltzmannDistribution bd = new BoltzmannDistribution(prefs, 1./beta);
		double [] probs = bd.getProbabilities();

		return computePolicyGradient(probs, prefs, grads, aind, beta);

	}

    public static FunctionGradient computePolicyGradient(double [] probs, double [] prefs, FunctionGradient[] grads, int aind, double beta){

        HashedAggregator<Integer> sums = new HashedAggregator<Integer>();

        //now get component for on action gradient
        FunctionGradient aterm = GradientUtils.scalarMultCopy(grads[aind], beta * (1. - probs[aind]));
        GradientUtils.sumInto(aterm, sums);

        //now sum over off action gradients
        for(int i = 0; i < prefs.length; i++){
            if(i == aind) continue;

            FunctionGradient offTerm = GradientUtils.scalarMultCopy(grads[i], -beta * probs[i]);
            GradientUtils.sumInto(offTerm, sums);
        }

        FunctionGradient unnormalized = GradientUtils.toGradient(sums);
        FunctionGradient grad = GradientUtils.scalarMultCopy(unnormalized, probs[aind]);

        return grad;

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
