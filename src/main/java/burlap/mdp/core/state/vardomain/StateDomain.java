package burlap.mdp.core.state.vardomain;

import burlap.mdp.core.state.State;

/**
 * An interface extension for when a {@link State} can specify the numeric domain of one or more of its variables.
 * @author James MacGlashan.
 */
public interface StateDomain extends State {


	/**
	 * Returns the numeric domain of the variable for the given key.
	 * @param key the key of the variable
	 * @return a {@link VariableDomain} specifying the domain of the variable. Null if undefined.
	 */
	VariableDomain domain(Object key);

}
