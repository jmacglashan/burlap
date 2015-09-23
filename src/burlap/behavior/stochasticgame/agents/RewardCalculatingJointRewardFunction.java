package burlap.behavior.stochasticgame.agents;

import java.util.HashMap;
import java.util.Map;

import burlap.behavior.stochasticgame.saconversion.RewardCalculator;
import burlap.oomdp.core.State;
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
			JointReward realRewardFunction, String agentName) {
		this.realRewardFunction = realRewardFunction;
		this.rewardCalcMap = rewardCalcMap;
		this.agentName = agentName;
	}


	@Override
	public Map<String, Double> reward(State s, JointAction ja, State sp) {

		Map<String, Double> calculatedRewards = new HashMap<String, Double>();

		Map<String, Double> realRewards = realRewardFunction.reward(s, ja, sp);
		for(String aName : realRewards.keySet()){
			//System.out.println("RC.AN: "+rewardCalcMap.get(aName));
			calculatedRewards.put(aName, 
					rewardCalcMap.get(aName).
					getReward(agentName, 
							realRewards));

		}

		return calculatedRewards;
	}

}
