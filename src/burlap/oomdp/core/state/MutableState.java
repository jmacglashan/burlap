package burlap.oomdp.core.state;

/**
 * A {@link State} interface extension for mutable states whose values can be directly modified. Requires implementing
 * the method {@link #set(Object, Object)} so variable keys can be set. If the {@link #copy()} performs shallow copies,
 * then the {@link #set(Object, Object)} method should perform a safe copy-on-write operation.
 * @author James MacGlashan.
 */
public interface MutableState extends State {

	/**
	 * Sets the value for the given variable key.
	 * @param variableKey the identifier for the variable value to change
	 * @param value the variable value to which the variable should be set.
	 * @return
	 */
	MutableState set(Object variableKey, Object value);

}
