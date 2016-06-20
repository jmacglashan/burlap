package burlap.mdp.stochasticgames.model;

import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.JointAction;

/**
 * This interface defines the method needed to return the reward received by each agent.
 * @author James MacGlashan
 *
 */
public interface JointRewardFunction {
	
	/**
	 * Returns the reward received by each agent specified in the joint action.
	 * @param s that state in which the joint action was taken.
	 * @param ja the joint action taken.
	 * @param sp the resulting state from taking the joint action
	 * @return a Map from agent names to the reward that they received.
	 */
	double[] reward(State s, JointAction ja, State sp);
}
