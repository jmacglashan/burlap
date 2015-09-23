package burlap.behavior.stochasticgames.saconversion;

import java.util.Map;

public abstract class RewardCalculator{

	protected String fnType;
	protected String agentName;

	public RewardCalculator(String fnType) {
		this.fnType = fnType;
	}
	
	public abstract double getReward(double myReward, double otherReward);

	public abstract String functionToString();
	
	@Override
	public String toString(){
		return this.fnType;
	}
	
	public String getAgentName(){
		return this.agentName;
	}

	public abstract double getReward(String agentNameIn,Map<String, Double> realRewards);
	
}
