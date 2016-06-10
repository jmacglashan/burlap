package burlap.behavior.singleagent.learnfromdemo.mlirl.support;

import burlap.behavior.valuefunction.QFunction;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;

import java.util.List;

/**
 * An interface for a valueFunction that can produce Q-value gradients.
 * @author James MacGlashan.
 */
public interface QGradientPlanner extends QFunction {


	/**
	 * Returns the list of Q-value gradients (returned as {@link burlap.behavior.singleagent.learnfromdemo.mlirl.support.QGradientTuple objects}) for each action permissible in the given state.
	 * @param s the state for which Q-value gradients are to be returned.
	 * @return the list of Q-value gradients for each action permissible in the given state.
	 */
	List<QGradientTuple> getAllQGradients(State s);


	/**
	 * Returns the Q-value gradient ({@link burlap.behavior.singleagent.learnfromdemo.mlirl.support.QGradientTuple}) for the given state and action.
	 * @param s the state for which the Q-value gradient is to be returned
	 * @param a the action for which the Q-value gradient is to be returned.
	 * @return the Q-value gradient for the given state and action.
	 */
	QGradientTuple getQGradient(State s, Action a);


}
