package burlap.oomdp.singleagent.common;

import java.util.List;

import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


public class SingleGoalPFRF implements RewardFunction {

	PropositionalFunction			pf;
	double							goalReward;
	double							nonGoalReward;
	
	
	
	public SingleGoalPFRF(PropositionalFunction pf){
		this.pf = pf;
		this.goalReward = 1.;
		this.nonGoalReward = 0.;
	}
	
	public SingleGoalPFRF(PropositionalFunction pf, double goalReward, double nonGoalReward){
		this.pf = pf;
		this.goalReward = goalReward;
		this.nonGoalReward = nonGoalReward;
	}
	
	
	@Override
	public double reward(State s, GroundedAction a, State sprime) {
		
		List<GroundedProp> gps = sprime.getAllGroundedPropsFor(pf);
		
		for(GroundedProp gp : gps){
			if(gp.isTrue(sprime)){
				return goalReward;
			}
		}
		
		return nonGoalReward;
	}

}
