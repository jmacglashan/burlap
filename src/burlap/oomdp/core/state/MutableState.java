package burlap.oomdp.core.state;

/**
 * A {@link State} interface extension for mutable states whose values can be directly modified. Requires implementing
 * the method {@link #set(Object, Object)} so variable keys can be set. Note that the super interface {@link #copy()}
 * method should return a copied state that has the property that changes made to copy do
 * not affect the values of the source state, so make sure copies are as deep as necessary.
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
