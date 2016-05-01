package burlap.oomdp.stochasticgames.common;

import burlap.oomdp.core.state.State;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.JointReward;

import java.util.HashMap;
import java.util.Map;

/**
 * A Joint reward function that always returns zero reward for each agent.
 * @author James MacGlashan.
 */
public class NullJointReward implements JointReward {

	@Override
	public Map<String, Double> reward(State s, JointAction ja, State sp) {
		HashMap<String, Double> rewards = new HashMap<String, Double>(ja.size());
		for(String agent : ja.getAgentNames()){
			rewards.put(agent, 0.);
		}
		return rewards;
	}
}
