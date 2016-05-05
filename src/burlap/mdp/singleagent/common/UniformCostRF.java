package burlap.mdp.singleagent.common;

import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.GroundedAction;
import burlap.mdp.singleagent.RewardFunction;


/**
 * Defines a reward function that always returns -1.
 * @author James MacGlashan
 *
 */
public class UniformCostRF implements RewardFunction {

	
	public UniformCostRF(){
		
	}
	
	@Override
	public double reward(State s, GroundedAction a, State sprime) {
		return -1;
	}

}
