package burlap.oomdp.singleagent.common;

import burlap.oomdp.core.oo.propositional.PropositionalFunction;
import burlap.oomdp.core.state.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


/**
 * This class defines a reward function that returns a goal reward when any grounded form of a propositional
 * function is true in the resulting state and a default non-goal reward otherwise.
 * @author James MacGlashan
 *
 */
public class SingleGoalPFRF implements RewardFunction {

	PropositionalFunction			pf;
	double							goalReward;
	double							nonGoalReward;
	
	
	
	/**
	 * Initializes the reward function to return 1 when any grounded from of pf is true in the resulting
	 * state.
	 * @param pf the propositional function that must have a true grounded version for the goal reward to be returned.
	 */
	public SingleGoalPFRF(PropositionalFunction pf){
		this.pf = pf;
		this.goalReward = 1.;
		this.nonGoalReward = 0.;
	}
	
	
	/**
	 * Initializes the reward function to return the specified goal reward when any grounded from of pf is true in the resulting
	 * state and the specified non-goal reward otherwise.
	 * @param pf the propositional function that must have a true grounded version for the goal reward to be returned.
	 * @param goalReward the goal reward value to be returned
	 * @param nonGoalReward the non goal reward value to be returned.
	 */
	public SingleGoalPFRF(PropositionalFunction pf, double goalReward, double nonGoalReward){
		this.pf = pf;
		this.goalReward = goalReward;
		this.nonGoalReward = nonGoalReward;
	}
	
	
	@Override
	public double reward(State s, GroundedAction a, State sprime) {
		
		if(this.pf.somePFGroundingIsTrue(sprime)){
			return goalReward;
		}
		return nonGoalReward;
		
		
	}

}
