package burlap.behavior.singleagent.planning.deterministic;

import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

public class UniformPlusGoalRF implements RewardFunction {

	protected StateConditionTest gc;
	
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
