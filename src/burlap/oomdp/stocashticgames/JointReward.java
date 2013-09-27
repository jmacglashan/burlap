package burlap.oomdp.stocashticgames;

import java.util.Map;

import burlap.oomdp.core.State;


public interface JointReward {
	public Map<String, Double> reward(State s, JointAction ja, State sp);
}
