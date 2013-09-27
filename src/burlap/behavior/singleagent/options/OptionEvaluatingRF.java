package burlap.behavior.singleagent.options;

import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

/*
 * This class is a reward function that accepts a reward function for primitive actions and returns
 * that when the query action is a primitive. If the query action is a option it
 * return the cumulative reward from the options last execution using the assumption that any options that need evaluating
 * have been set to internally keep track of their reward after each successive application. It also
 * is assumed that those options are using the same reward function as the inputed primitive RF
 * 
 * This is useful for planners that would want to execute the option and evaluate the reward afterwards
 * 
*/

public class OptionEvaluatingRF implements RewardFunction {

	RewardFunction primitiveRF;
	
	public OptionEvaluatingRF(RewardFunction rf){
		this.primitiveRF = rf;
	}
	

	@Override
	public double reward(State s, GroundedAction a, State sprime) {
		
		if(a.action.isPrimitive()){
			return primitiveRF.reward(s, a, sprime);
		}
		
		//otherwise return the cumulative reward from the last option execution
		//with the assumption that the last call to the option produced this SAS tuple
		Option o = (Option)a.action;
		return o.getLastCumulativeReward();

	}

}
