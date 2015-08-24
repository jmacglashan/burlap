package burlap.behavior.stochasticgame.saconversion;

public class SelfishRewardCalculator extends OtherRegardingRewardCalculator{

	public SelfishRewardCalculator() {
		super(0, 0);
		this.fnType = "Selfish";
	}

	@Override
	public double getReward(double myReward, double otherReward) {
		return super.getReward(myReward, otherReward);
	}

	@Override
	public String functionToString() {
		return "R = RewardFromGameWorld";
	}

}
