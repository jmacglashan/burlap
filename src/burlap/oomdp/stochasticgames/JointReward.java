package burlap.oomdp.stochasticgames;

import java.util.Map;

import burlap.oomdp.core.state.State;

/**
 * This interface defines the method needed to return the reward received by each agent.
 * @author James MacGlashan
 *
 */
public interface JointReward {
	
	/**
	 * Returns the reward received by each agent specified in the joint action. The returned
	 * result is a Map from agent names to the reward that they received.
	 * @param s that state in which the joint action was taken.
	 * @param ja the joint action taken.
	 * @param sp the resulting state from taking the joint action
	 * @return a Map from agent names to the reward that they received.
	 */
	public Map<String, Double> reward(State s, JointAction ja, State sp);
}
