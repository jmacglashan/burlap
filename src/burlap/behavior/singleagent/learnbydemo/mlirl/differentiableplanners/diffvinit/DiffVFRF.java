package burlap.behavior.singleagent.learnbydemo.mlirl.differentiableplanners.diffvinit;

import burlap.behavior.singleagent.learnbydemo.mlirl.support.DifferentiableRF;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

/**
 * A differentiable reward function wrapper for use with {@link burlap.behavior.singleagent.learnbydemo.mlirl.MLIRL} when
 * the reward function is known, but the value function initialization for leaf nodes is to be learned.
 * This class takes as input the true reward function and a {@link burlap.behavior.singleagent.learnbydemo.mlirl.differentiableplanners.diffvinit.DifferentiableVInit}
 * object to form the {@link burlap.behavior.singleagent.learnbydemo.mlirl.support.DifferentiableRF} object
 * that {@link burlap.behavior.singleagent.learnbydemo.mlirl.MLIRL} will use.
 *
 * @author James MacGlashan.
 */
public class DiffVFRF extends DifferentiableRF {

	protected RewardFunction objectiveRF;
	protected DifferentiableVInit.ParamedDiffVInit diffVInit;


	public DiffVFRF(RewardFunction objectiveRF, DifferentiableVInit.ParamedDiffVInit diffVinit){
		this.objectiveRF = objectiveRF;
		this.diffVInit = diffVinit;

		this.dim = diffVinit.getParameterDimension();
		this.parameters = diffVinit.getParameters();
	}

	@Override
	public double[] getGradient(State s, GroundedAction ga, State sp) {
		return new double[this.dim];
	}

	@Override
	protected DifferentiableRF copyHelper() {
		return null;
	}

	@Override
	public double reward(State s, GroundedAction a, State sprime) {
		return this.objectiveRF.reward(s, a, sprime);
	}


	@Override
	public void setParameters(double[] parameters) {
		super.setParameters(parameters);
		this.diffVInit.setParameters(parameters);
	}

}
