package burlap.behavior.singleagent.options.support;

import burlap.oomdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


/**
 * It is typical for options to be defined for following policies to subgoals and it is often useful
 * to use a planning or learning algorithm to define these policies, in which case a subgoal reward
 * function for the option would need to be specified. This reward function 
 * defines a set of states in which an option is applicable and the subgoal states of the option.
 * The subgoal state and applicable states are specified using {@link burlap.oomdp.auxiliary.stateconditiontest.StateConditionTest}
 * objects. By default, a subgoal reward of 0 is returned for transitions to the subgoal states, the most negative value
 * for transitions to states in which the option is not applicable and -1 for any other transitions. All of these
 * values can be changed.
 * @author James MacGlashan
 *
 */
public class LocalSubgoalRF implements RewardFunction {

	
	/**
	 * Defines the set of states in which the option is applicable
	 */
	protected StateConditionTest		applicableStateTest;
	
	/**
	 * Defines he set of subgoal states for the option
	 */
	protected StateConditionTest		subgoalStateTest;
	
	
	/**
	 * Defines the reward returned for transitions to subgoal states; default 0.
	 */
	protected double					subgoalReward = 0;
	
	/**
	 * Defines the reward returned for transitions to applicable states, but not subgoal states; default -1
	 */
	protected double					defaultReward = -1;
	
	/**
	 * Defines the reward returned for transitions to states in which the option is not applicable; default -Double.MAX_VALUE
	 */
	protected double					failReward = -Double.MAX_VALUE;
	
	
	/**
	 * Initializes with a given set of subgoal states. The set of applicable states is assumed to be every state.
	 * @param subgoalStateTest the subgoal states
	 */
	public LocalSubgoalRF(StateConditionTest subgoalStateTest) {
		this.applicableStateTest = null;
		this.subgoalStateTest = subgoalStateTest;
	}
	
	
	/**
	 * Initializes with a given set of subgoal states, a default reward and a subgoal reward. The set of applicable states for the option is assumed to be every state.
	 * @param subgoalStateTest the subgoal states
	 * @param defaultReward the default reward
	 * @param subgoalReward the reward returned for transitioning to subgoal states
	 */
	public LocalSubgoalRF(StateConditionTest subgoalStateTest, double defaultReward, double subgoalReward) {
		this.applicableStateTest = null;
		this.subgoalStateTest = subgoalStateTest;
		
		this.defaultReward = defaultReward;
		this.subgoalReward = subgoalReward;
	}
	
	
	/**
	 * Initializes with a set of states in which an option is applicable and which the agent should not enter and a set of 
	 * subgoal states
	 * @param applicableStateTest the applicable states. Transitioning to a non-applicable state causes a reward of <code>failReward</code>.
	 * @param subgoalStateTest the subgoal states
	 */
	public LocalSubgoalRF(StateConditionTest applicableStateTest, StateConditionTest subgoalStateTest) {
		this.applicableStateTest = applicableStateTest;
		this.subgoalStateTest = subgoalStateTest;
	}
	
	
	/**
	 * Initializes
	 * @param applicableStateTest Defines the set of states in which the option is applicable. Transitioning to a non-applicable state causes a reward of <code>failReward</code>.
	 * @param subgoalStateTest the subgoal states
	 * @param defaultReward the default reward
	 * @param failReward the reward for transitioning to a non-subgoal non-applicable state
	 * @param subgoalReward the reward returned for transitioning to subgoal states
	 */
	public LocalSubgoalRF(StateConditionTest applicableStateTest, StateConditionTest subgoalStateTest, double defaultReward, 
			double failReward, double subgoalReward) {
		
		this.applicableStateTest = applicableStateTest;
		this.subgoalStateTest = subgoalStateTest;
		
		this.defaultReward = defaultReward;
		this.failReward = failReward;
		this.subgoalReward = subgoalReward;
		
	}
	
	

	@Override
	public double reward(State s, GroundedAction a, State sprime) {
		
		if(this.subgoalStateTest.satisfies(sprime)){
			return this.subgoalReward;
		}
		
		if(this.applicableStateTest != null){
			if(!this.applicableStateTest.satisfies(sprime)){
				return failReward; //agent has exited option range without achieving goal
			}
		}
		
		return defaultReward;
	}

}
