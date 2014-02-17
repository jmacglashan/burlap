package burlap.behavior.singleagent.learning;

import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


/**
 * A reward function implementation designed around goal conditions that are specified by a {@link burlap.behavior.singleagent.planning.StateConditionTest} object.
 * When the agent transition to a state marked as a goal state, it returns a goal reward. Otherwise a default reward is returned.
 * @author James MacGlashan
 *
 */
public class GoalBasedRF implements RewardFunction {

	protected StateConditionTest gc;
	protected double goalReward = 1.0;
	protected double defaultReward = 0.;
	
	/**
	 * Initializes with transitions to goal states returning a reward of 1 and all others returning 0
	 * @param gc {@link burlap.behavior.singleagent.planning.StateConditionTest} object that specifies goal states. 
	 */
	public GoalBasedRF(StateConditionTest gc) {
		this.gc = gc;
	}

	
	/**
	 * Initializes with transitions to goal states returning the give reward and all others returning 0.
	 * @param gc {@link burlap.behavior.singleagent.planning.StateConditionTest} object that specifies goal states.
	 * @param goalReward the reward returned for transitions to goal states.
	 */
	public GoalBasedRF(StateConditionTest gc, double goalReward) {
		this.gc = gc;
		this.goalReward = goalReward;
	}
	
	
	/**
	 * Initializes with transitions to goal states returning the give reward and all others returning 0.
	 * @param gc {@link burlap.behavior.singleagent.planning.StateConditionTest} object that specifies goal states.
	 * @param goalReward the reward returned for transitions to goal states.
	 * @param defaultReward the default reward returned for all non-goal state transitions.
	 */
	public GoalBasedRF(StateConditionTest gc, double goalReward, double defaultReward) {
		this.gc = gc;
		this.goalReward = goalReward;
		this.defaultReward = defaultReward;
	}

	@Override
	public double reward(State s, GroundedAction a, State sprime) {
		
		if(gc.satisfies(sprime)){
			return goalReward;
		}
		
		return defaultReward;
	}

}
