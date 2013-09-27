package burlap.behavior.singleagent.options;

import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.RewardFunction;

public interface DirectOptionTerminateMapper {

	public State generateOptionTerminalState(State s);
	public int getNumSteps(State s, State sp);
	public double getCumulativeReward(State s, State sp, RewardFunction rf, double discount);
	
}
