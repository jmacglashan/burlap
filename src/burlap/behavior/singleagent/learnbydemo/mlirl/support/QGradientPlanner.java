package burlap.behavior.singleagent.learnbydemo.mlirl.support;

import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;

import java.util.List;

/**
 * An interface for a planner that can produce Q-value gradients.
 * @author James MacGlashan.
 */
public interface QGradientPlanner extends QComputablePlanner{


	/**
	 * Returns the list of Q-value gradients (returned as {@link burlap.behavior.singleagent.learnbydemo.mlirl.support.QGradientTuple objects}) for each action permissible in the given state.
	 * @param s the state for which Q-value gradients are to be returned.
	 * @return the list of Q-value gradients for each action permissible in the given state.
	 */
	public List<QGradientTuple> getAllQGradients(State s);


	/**
	 * Returns the Q-value gradient ({@link burlap.behavior.singleagent.learnbydemo.mlirl.support.QGradientTuple}) for the given state and action.
	 * @param s the state for which the Q-value gradient is to be returned
	 * @param a the action for which the Q-value gradient is to be returned.
	 * @return the Q-value gradient for the given state and action.
	 */
	public QGradientTuple getQGradient(State s, GroundedAction a);


	/**
	 * Sets this planner's Boltzmann beta parameter used to compute gradients. As beta gets larger, the policy becomes more deterministic.
	 * @param beta the value to which this planner's Boltzmann beta parameter will be set
	 */
	public void setBoltzmannBetaParameter(double beta);

}
