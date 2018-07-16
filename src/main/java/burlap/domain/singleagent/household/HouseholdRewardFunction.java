package household;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.common.GoalBasedRF;

public class HouseholdRewardFunction extends GoalBasedRF {

    private double noopReward;
    
    private double TerminalFunction tf;

    public HouseholdRewardFunction(HouseholdGoal goal,
				   double rewardGoal,
				   double rewardDefault,
				   double rewardNoop)
    {
	super(goal, rewardGoal, rewardDefault);
	this.noopReward = rewardNoop;
    }

    @Override
    public double reward(State s, Action a, State sprime) {
	double superR = super.reward(s, a, sprime);
	double r = superR;

	if(s.equals(sprime)) {
	    r += noopReward;
	}

	// check to see if there was a violation
	// if(isViolation(s, a, sprime)) { 
	//   if there was, include a violation penalty
	
	return r;
    }
}

