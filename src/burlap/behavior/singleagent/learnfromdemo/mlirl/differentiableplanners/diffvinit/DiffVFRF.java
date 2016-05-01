package burlap.behavior.singleagent.learnfromdemo.mlirl.differentiableplanners.diffvinit;

import burlap.behavior.singleagent.learnfromdemo.mlirl.support.DifferentiableRF;
import burlap.behavior.singleagent.vfa.FunctionGradient;
import burlap.behavior.singleagent.vfa.ParametricFunction;
import burlap.oomdp.core.state.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

/**
 * A differentiable reward function wrapper for use with {@link burlap.behavior.singleagent.learnfromdemo.mlirl.MLIRL} when
 * the reward function is known, but the value function initialization for leaf nodes is to be learned.
 * This class takes as input the true reward function and a {@link burlap.behavior.singleagent.learnfromdemo.mlirl.differentiableplanners.diffvinit.DifferentiableVInit}
 * object to form the {@link burlap.behavior.singleagent.learnfromdemo.mlirl.support.DifferentiableRF} object
 * that {@link burlap.behavior.singleagent.learnfromdemo.mlirl.MLIRL} will use.
 *
 * @author James MacGlashan.
 */
public class DiffVFRF implements DifferentiableRF {

	protected RewardFunction objectiveRF;
	protected DifferentiableVInit diffVInit;

	protected int dim;


	public DiffVFRF(RewardFunction objectiveRF, DifferentiableVInit diffVinit){
		this.objectiveRF = objectiveRF;
		this.diffVInit = diffVinit;

		this.dim = diffVinit.numParameters();
	}

	@Override
	public FunctionGradient gradient(State s, GroundedAction a, State sprime) {
		return new FunctionGradient.SparseGradient();
	}

	@Override
	public int numParameters() {
		return this.diffVInit.numParameters();
	}

	@Override
	public double getParameter(int i) {
		return this.diffVInit.getParameter(i);
	}

	@Override
	public void setParameter(int i, double p) {
		this.diffVInit.setParameter(i, p);
	}

	@Override
	public void resetParameters() {
		this.diffVInit.resetParameters();
	}

	@Override
	public ParametricFunction copy() {
		return new DiffVFRF(this.objectiveRF, (DifferentiableVInit)this.diffVInit.copy());
	}


	@Override
	public double reward(State s, GroundedAction a, State sprime) {
		return this.objectiveRF.reward(s, a, sprime);
	}



}
