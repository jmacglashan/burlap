package burlap.oomdp.singleagent.common;

import burlap.oomdp.core.state.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

/**
 * This class defines a reward function that always returns 0
 * @author James MacGlashan
 *
 */
public class NullRewardFunction implements RewardFunction {

	@Override
	public double reward(State s, GroundedAction a, State sprime) {
		return 0;
	}

}
