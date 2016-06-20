package burlap.mdp.core.oo.state;

import burlap.mdp.core.state.State;

/**
 * A {@link State} extension for objects that are OO-MDP objects of an {@link OOState}
 * @author James MacGlashan.
 */
public interface ObjectInstance extends State {

	/**
	 * Returns the name of this OO-MDP object class
	 * @return the name of this OO-MDP object class
	 */
	String className();

	/**
	 * Returns the name of this object instance
	 * @return the name of this object instance
	 */
	String name();

	/**
	 * Returns a copy of this {@link ObjectInstance} with the specified name
	 * @param objectName the new name for the object
	 * @return a copy of this object
	 */
	ObjectInstance copyWithName(String objectName);
}
