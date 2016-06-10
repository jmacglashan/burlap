package burlap.mdp.singleagent.common;

import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.auxiliary.stateconditiontest.TFGoalCondition;
import burlap.mdp.core.Action;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;


/**
 * A reward function implementation designed around goal conditions that are specified by a {@link burlap.mdp.auxiliary.stateconditiontest.StateConditionTest} object
 * or a {@link burlap.mdp.core.TerminalFunction}.
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
	 * @param gc {@link burlap.mdp.auxiliary.stateconditiontest.StateConditionTest} object that specifies goal states.
	 */
	public GoalBasedRF(StateConditionTest gc) {
		this.gc = gc;
	}

	
	/**
	 * Initializes with transitions to goal states returning the given reward and all others returning 0.
	 * @param gc {@link burlap.mdp.auxiliary.stateconditiontest.StateConditionTest} object that specifies goal states.
	 * @param goalReward the reward returned for transitions to goal states.
	 */
	public GoalBasedRF(StateConditionTest gc, double goalReward) {
		this.gc = gc;
		this.goalReward = goalReward;
	}
	
	
	/**
	 * Initializes with transitions to goal states returning the given reward and all others returning 0.
	 * @param gc {@link burlap.mdp.auxiliary.stateconditiontest.StateConditionTest} object that specifies goal states.
	 * @param goalReward the reward returned for transitions to goal states.
	 * @param defaultReward the default reward returned for all non-goal state transitions.
	 */
	public GoalBasedRF(StateConditionTest gc, double goalReward, double defaultReward) {
		this.gc = gc;
		this.goalReward = goalReward;
		this.defaultReward = defaultReward;
	}
	
	
	
	
	/**
	 * Initializes with transitions to goal states, indicated by the terminal function, returning a reward of 1 and all others returning 0
	 * @param tf {@link TerminalFunction} object that specifies goal states. 
	 */
	public GoalBasedRF(TerminalFunction tf) {
		this(new TFGoalCondition(tf));
	}

	
	/**
	 * Initializes with transitions to goal states, indicated by the terminal function, returning the given reward and all others returning 0.
	 * @param tf {@link TerminalFunction} object that specifies goal states.
	 * @param goalReward the reward returned for transitions to goal states.
	 */
	public GoalBasedRF(TerminalFunction tf, double goalReward) {
		this(new TFGoalCondition(tf), goalReward);
	}
	
	
	/**
	 * Initializes with transitions to goal states, indicated by the terminal function, returning the given reward and all others returning 0.
	 * @param tf {@link TerminalFunction} object that specifies goal states.
	 * @param goalReward the reward returned for transitions to goal states.
	 * @param defaultReward the default reward returned for all non-goal state transitions.
	 */
	public GoalBasedRF(TerminalFunction tf, double goalReward, double defaultReward) {
		this(new TFGoalCondition(tf), goalReward, defaultReward);
	}
	
	
	/**
	 * Returns the goal condition for this reward function.
	 * @return the goal condition for this reward function.
	 */
	public StateConditionTest getGoalCondition(){
		return this.gc;
	}


	public double getGoalReward() {
		return goalReward;
	}

	public void setGoalReward(double goalReward) {
		this.goalReward = goalReward;
	}

	public double getDefaultReward() {
		return defaultReward;
	}

	public void setDefaultReward(double defaultReward) {
		this.defaultReward = defaultReward;
	}

	@Override
	public double reward(State s, Action a, State sprime) {
		
		if(gc.satisfies(sprime)){
			return goalReward;
		}
		
		return defaultReward;
	}

}
