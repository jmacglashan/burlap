package burlap.mdp.core;

import burlap.mdp.core.state.State;

/**
 *
 * @author James MacGlashan
 *
 */
public interface Action {

	
	/**
	 * Returns the action name for this grounded action.
	 * @return the action name for this grounded action.
	 */
	String actionName();

	
	/**
	 * Returns a copy of this grounded action.
	 * @return a copy of this grounded action.
	 */
	Action copy();
	
	
	boolean applicableInState(State s);


	
}
