package burlap.behavior.stochasticgames.saconversion;

import java.util.Map;

public class OtherRegardingRewardCalculator extends RewardCalculator {

	private double coopParam, defParam;

	public OtherRegardingRewardCalculator(double cooperateParam,
			double defenseParam) {
		super("OtherRegarding");
		this.coopParam = cooperateParam;
		this.defParam = defenseParam;
	}

	@Override
	public double getReward(double myReward, double otherReward) {

		if (otherReward > 0 || myReward > 0) {
			return myReward + coopParam * otherReward - defParam
					* Math.max(0, otherReward - myReward);
		} else {
			return myReward;
		}

	}

	@Override
	public String functionToString() {
		return "R = myR + ( "+this.coopParam+" * otherR ) - ("+this.defParam+" * max( 0, otherR - myR))";
	}

	@Override
	public double getReward(String agentNameIn,
			Map<String, Double> realRewards) {
		double otherReward =0;
		System.out.println("RR: "+realRewards);
		System.out.println("AN: "+agentNameIn);
		double myReward = realRewards.get(agentNameIn);
		for(String oAgentName : realRewards.keySet()){
			
			if(oAgentName!=agentNameIn){
				otherReward+=realRewards.get(oAgentName);
			}
			
		}
		if (otherReward > 0 || myReward > 0) {
			return myReward + coopParam * otherReward - defParam
					* Math.max(0, otherReward - myReward);
		} else {
			return myReward;
		}
	}

}
