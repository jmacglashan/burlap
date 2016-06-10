package burlap.mdp.core.state;

/**
 * A {@link State} interface extension for mutable states whose values can be directly modified. Requires implementing
 * the method {@link #set(Object, Object)} so variable values for the input key can be set. If the {@link State} {@link #copy()}
 * method performs shallow copies, and the variable is a non-primitive data field,
 * then the {@link #set(Object, Object)} method should perform a safe copy-on-write operation so that the {@link State}
 * object from which the state was copied is not modified.
 * @author James MacGlashan.
 */
public interface MutableState extends State {

	/**
	 * Sets the value for the given variable key.
	 * @param variableKey the key identifier for the variable value to change
	 * @param value the variable value to which the variable should be set.
	 * @return this object to support method chaining
	 */
	MutableState set(Object variableKey, Object value);

}
