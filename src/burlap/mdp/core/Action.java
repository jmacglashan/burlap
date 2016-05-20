package burlap.mdp.core;

/**
 * An interface for action definitions. Should be able to return a unique action name and have copies made.
 * Implementations of this interface should primarily hold data defining any action parameters.
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
