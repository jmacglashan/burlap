package burlap.oomdp.singleagent.common;

import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

public class NullRewardFunction implements RewardFunction {

	@Override
	public double reward(State s, GroundedAction a, State sprime) {
		return 0;
	}

}
