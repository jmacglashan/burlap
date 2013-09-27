package burlap.oomdp.singleagent;

import burlap.oomdp.core.State;

public interface RewardFunction {
	
	//note that params are the parameters for the action
	public abstract double reward(State s, GroundedAction a, State sprime);

}
