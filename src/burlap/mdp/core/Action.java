package burlap.mdp.core;

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


}
