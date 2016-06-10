package burlap.mdp.stochasticgames.model;

import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.action.JointAction;

/**
 * The interface and comment mechanisms for defining
 * a stochastic game's state transition dynamics.
 * 
 * @author James MacGlashan
 *
 */
public interface JointModel {

	
	/**
	 * Samples the result of performing {@link JointAction} ja in {@link State} s.
	 * @param s the state in which the joint action is performed.
	 * @param ja the joint action to be performed
	 * @return a sample from the resulting state.
	 */
	State sample(State s, JointAction ja);



}
