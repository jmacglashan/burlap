package burlap.mdp.stochasticgames.common;

import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.model.JointRewardFunction;

/**
 * A Joint reward function that always returns zero reward for each agent.
 * @author James MacGlashan.
 */
public class NullJointRewardFunction implements JointRewardFunction {

	@Override
	public double[] reward(State s, JointAction ja, State sp) {
		double [] r = new double[ja.size()];
		return r;
	}
}
