package burlap.oomdp.core.oo.state;

import burlap.oomdp.core.state.State;

/**
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
	 */
	ObjectInstance copyWithName(String objectName);
}
