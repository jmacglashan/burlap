package burlap.mdp.singleagent.common;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;

/**
 * This class defines a reward function that always returns 0
 * @author James MacGlashan
 *
 */
public class NullRewardFunction implements RewardFunction {

	@Override
	public double reward(State s, Action a, State sprime) {
		return 0;
	}

}
