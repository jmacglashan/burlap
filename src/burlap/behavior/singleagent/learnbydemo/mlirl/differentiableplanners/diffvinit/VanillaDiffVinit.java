package burlap.behavior.singleagent.learnbydemo.mlirl.differentiableplanners.diffvinit;

import burlap.behavior.valuefunction.ValueFunctionInitialization;
import burlap.behavior.singleagent.learnbydemo.mlirl.support.DifferentiableRF;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;

/**
 * A class for the default condition when a value function initialization returns an unparameterized  value
 * for each state, but must be differentiable
 * with respect to the reward function parameters for use with a differentiable finite horizon planner.
 * @author James MacGlashan.
 */
public class VanillaDiffVinit implements DifferentiableVInit {


	/**
	 * The source value function initialization.
	 */
	protected ValueFunctionInitialization vinit;

	/**
	 * The differentiable reward function that defines the parameter space over which this value function
	 * initialization must differentiate.
	 */
	protected DifferentiableRF rf;


	/**
	 * Initializes.
	 * @param vinit The vanilla unparameterized value function initialization
	 * @param rf the differentiable reward function that defines the total parameter space
	 */
	public VanillaDiffVinit(ValueFunctionInitialization vinit, DifferentiableRF rf) {
		this.vinit = vinit;
		this.rf = rf;
	}

	@Override
	public double[] getVGradient(State s) {
		return new double[rf.getParameterDimension()];
	}

	@Override
	public double[] getQGradient(State s, AbstractGroundedAction ga) {
		return new double[rf.getParameterDimension()];
	}

	@Override
	public double value(State s) {
		return this.vinit.value(s);
	}

	@Override
	public double qValue(State s, AbstractGroundedAction a) {
		return this.vinit.qValue(s, a);
	}

}
