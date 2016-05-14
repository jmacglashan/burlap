package burlap.behavior.singleagent.learnfromdemo.mlirl.support;

import burlap.behavior.functionapproximation.FunctionGradient;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;

/**
 * A tuple (triple) for storing the Q-gradient associated with a state and action.
 * @author James MacGlashan.
 */
public class QGradientTuple {

	/**
	 * The state
	 */
	public State s;

	/**
	 * The action
	 */
	public Action a;

	/**
	 * The gradient for the state and action.
	 */
	public FunctionGradient gradient;


	/**
	 * Initializes.
	 * @param s the state
	 * @param a the action
	 * @param gradient the gradient for the state an action
	 */
	public QGradientTuple(State s, Action a, FunctionGradient gradient){
		this.s = s;
		this.a = a;
		this.gradient = gradient;
	}

}
