package burlap.behavior.stochasticgames.agents;

import java.util.HashMap;
import java.util.Map;

import burlap.behavior.stochasticgames.saconversion.RewardCalculator;
import burlap.domain.stochasticgames.gridgame.GridGame.GGJointRewardFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.JointReward;

/**
 * 
 * @author Betsy Hilliard betsy@cs.brown.edu
 *
 */
public class RewardCalculatingJointRewardFunction implements JointReward {



	private Map<String,RewardCalculator> rewardCalcMap;
	private JointReward realRewardFunction;
	private String agentName;

	public RewardCalculatingJointRewardFunction(Map<String,RewardCalculator> rewardCalcMap, 
			JointReward jointReward, String agentName) {
		this.realRewardFunction = jointReward;
		this.rewardCalcMap = rewardCalcMap;
		this.agentName = agentName;
	}


	@Override
	public Map<String, Double> reward(State s, JointAction ja, State sp) {

		
		Map<String, Double> realRewards = realRewardFunction.reward(s, ja, sp);
		Map<String, Double> calculatedRewards = new HashMap<String, Double>(realRewards.size() * 2);

		
		for(String aName : realRewards.keySet()){
			//System.out.println("RC.AN: "+rewardCalcMap.get(aName));
			calculatedRewards.put(aName, 
					rewardCalcMap.get(aName).
					getReward(agentName, 
							realRewards));

		}

		return calculatedRewards;
	}
	
	public double getRewardFor(State s, JointAction ja, State sp, String agent) {
		Map<String, Double> realRewards = realRewardFunction.reward(s, ja, sp);
		double propCalculated = rewardCalcMap.get(agent).getReward(agentName, realRewards);
		return propCalculated;
	}

}
