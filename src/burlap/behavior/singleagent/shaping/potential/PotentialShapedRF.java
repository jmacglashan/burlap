package burlap.behavior.singleagent.shaping.potential;

import burlap.behavior.singleagent.shaping.ShapedRewardFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


/**
 * This class is used to implement Potential-based reward shaping [1] which is guaranteed to preserve the optimal policy. This class
 * requires a {@link PotentialFunction} and the discount being used by the MDP. The additive reward is defined as:
 * d * p(s') - p(s)
 * where d is this discount factor, s' is the most recent state, s is the previous state, and p(s) is the potential of state s.
 * 
 * 
 * 1. Ng, Andrew Y., Daishi Harada, and Stuart Russell. "Policy invariance under reward transformations: Theory and application to reward shaping." ICML. 1999.
 * 
 * @author James MacGlashan
 *
 */
public class PotentialShapedRF extends ShapedRewardFunction {

	
	/**
	 * The potential function that can be used to return the potential reward from input states.
	 */
	protected PotentialFunction			potentialFunction;
	
	/**
	 * The discount factor the MDP (required for this to shaping to preserve policy optimality)
	 */
	protected double					discount;
	
	
	/**
	 * Initializes the shaping with the objective reward function, the potential function, and the discount of the MDP.
	 * @param baseRF the objective task reward function.
	 * @param potentialFunction the potential function to use.
	 * @param discount the discount factor of the MDP.
	 */
	public PotentialShapedRF(RewardFunction baseRF, PotentialFunction potentialFunction, double discount) {
		super(baseRF);
		
		this.potentialFunction = potentialFunction;
		this.discount = discount;
		
	}

	@Override
	public double additiveReward(State s, GroundedAction a, State sprime) {
		return (this.discount * this.potentialFunction.potentialValue(sprime)) - this.potentialFunction.potentialValue(s);
	}

}
