package burlap.behavior.singleagent.shaping;

import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;


/**
 * This abstract class is used to define shaped reward functions. Shaped reward functions take the base
 * true objective reward function of a task and add some additional reward value to it that helps suggest
 * useful states. Subclasses of the ShapedRewardFunction must implement a method that specifies the
 * additive reward to the base reward.
 * 
 * 
 * @author James MacGlashan
 *
 */
public abstract class ShapedRewardFunction implements RewardFunction {

	
	/**
	 * The base objective reward function for the task.
	 */
	protected RewardFunction		baseRF;
	
	
	/**
	 * Returns the reward value to add to the base objective reward function.
	 * @param s the previous state
	 * @param a the action taken the previous state
	 * @param sprime the successor state
	 * @return the reward value to add to the base objective reward function.
	 */
	public abstract double additiveReward(State s, Action a, State sprime);
	
	
	/**
	 * Initializes with the base objective task reward function.
	 * @param baseRF the objective task reward function.
	 */
	public ShapedRewardFunction(RewardFunction baseRF) {
		this.baseRF = baseRF;
	}
	
	@Override
	public double reward(State s, Action a, State sprime) {
		return this.baseRF.reward(s, a, sprime) + this.additiveReward(s, a, sprime);
	}

}
