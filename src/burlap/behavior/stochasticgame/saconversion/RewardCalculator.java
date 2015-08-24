package burlap.behavior.stochasticgame.saconversion;

public abstract class RewardCalculator{

	protected String fnType;

	public RewardCalculator(String fnType) {
		this.fnType = fnType;
	}
	
	public abstract double getReward(double myReward, double otherReward);

	public abstract String functionToString();
	
	@Override
	public String toString(){
		return this.fnType;
	}
	
}
