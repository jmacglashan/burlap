package burlap.behavior.singleagent.learnfromdemo.mlirl.support;

import burlap.behavior.functionapproximation.FunctionGradient;
import burlap.behavior.valuefunction.QFunction;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;

/**
 * An interface for a valueFunction that can produce Q-value gradients.
 * @author James MacGlashan.
 */
public interface DifferentiableQFunction extends QFunction {


	/**
	 * Returns the Q-value gradient ({@link burlap.behavior.singleagent.learnfromdemo.mlirl.support.QGradientTuple}) for the given state and action.
	 * @param s the state for which the Q-value gradient is to be returned
	 * @param a the action for which the Q-value gradient is to be returned.
	 * @return the Q-value gradient for the given state and action.
	 */
	FunctionGradient qGradient(State s, Action a);


}
