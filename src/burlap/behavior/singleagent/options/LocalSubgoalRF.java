package burlap.behavior.singleagent.options;

import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

public class LocalSubgoalRF implements RewardFunction {

	protected StateConditionTest		applicableStateTest;
	protected StateConditionTest		subgoalStateTest;
	
	protected double					subgoalReward = 0;
	protected double					defaultReward = -1;
	protected double					failReward = -Double.MAX_VALUE;
	
	public LocalSubgoalRF(StateConditionTest subgoalStateTest) {
		this.applicableStateTest = null;
		this.subgoalStateTest = subgoalStateTest;
	}
	
	public LocalSubgoalRF(StateConditionTest subgoalStateTest, double defaultReward, double subgoalReward) {
		this.applicableStateTest = null;
		this.subgoalStateTest = subgoalStateTest;
		
		this.defaultReward = defaultReward;
		this.subgoalReward = subgoalReward;
	}
	
	public LocalSubgoalRF(StateConditionTest applicableStateTest, StateConditionTest subgoalStateTest) {
		this.applicableStateTest = applicableStateTest;
		this.subgoalStateTest = subgoalStateTest;
	}
	
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
