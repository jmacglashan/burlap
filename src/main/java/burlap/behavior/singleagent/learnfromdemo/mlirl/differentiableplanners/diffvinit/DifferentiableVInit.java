package burlap.behavior.singleagent.learnfromdemo.mlirl.differentiableplanners.diffvinit;

import burlap.behavior.functionapproximation.FunctionGradient;
import burlap.behavior.functionapproximation.ParametricFunction;
import burlap.behavior.valuefunction.ValueFunctionInitialization;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;

/**
 * An interface for value function initialization that is differentiable with respect to some parameters. This
 * interface is useful for DifferentiableSparseSampling which may be used to learn the value of leaf nodes
 * in a finite horizon valueFunction.
 *
 * @author James MacGlashan.
 */
public interface DifferentiableVInit extends ValueFunctionInitialization, ParametricFunction {

	/**
	 * Returns the value function gradient.
	 * @param s the state on which the value function is to be evaluated
	 * @return the value function gradient.
	 */
	public FunctionGradient getVGradient(State s);


	/**
	 * Returns the Q-value function gradient.
	 * @param s the state on which the Q-value is to be evaluated.
	 * @param ga the action on which the Q-value is to be evaluated.
	 * @return the Q-value function gradient
	 */
	public FunctionGradient getQGradient(State s, Action ga);


}
