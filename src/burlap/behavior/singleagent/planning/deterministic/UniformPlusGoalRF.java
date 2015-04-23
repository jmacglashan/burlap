package burlap.behavior.singleagent.planning.deterministic;

import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


/**
 * This Reward function returns a uniform cost (-1) for all transitions that do not transition to a goal
 * state and 0 on transitions to the goal state. Goal states are indicated by a StateConditionTest class.
 * @author James MacGlashan
 *
 */
public class UniformPlusGoalRF implements RewardFunction {

	protected StateConditionTest gc;
	
	
	/**
	 * Sets the reward function to return 0 when transition to states that satisfy gc, and -1 otherwise
	 * @param gc when gc returns true, it indicates a goal state.
	 */
	public UniformPlusGoalRF(StateConditionTest gc){
		this.gc = gc;
	}
	
	@Override
	public double reward(State s, GroundedAction a, State sprime) {
		if(!gc.satisfies(sprime)){
			return -1;
		}
		return 0;
	}

}
