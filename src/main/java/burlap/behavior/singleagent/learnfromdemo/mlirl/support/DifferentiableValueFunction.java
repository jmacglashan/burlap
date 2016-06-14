package burlap.behavior.singleagent.learnfromdemo.mlirl.support;

import burlap.behavior.functionapproximation.FunctionGradient;
import burlap.behavior.valuefunction.ValueFunction;
import burlap.mdp.core.state.State;

/**
 * @author James MacGlashan.
 */
public interface DifferentiableValueFunction extends ValueFunction{
	/**
	 * Returns the gradient of this value function
	 * @param s the state on which the function is to be evaluated
	 * @return the gradient of this value function
	 */
	FunctionGradient valueGradient(State s);
}
