package burlap.behavior.stochasticgame.saconversion;

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

}
