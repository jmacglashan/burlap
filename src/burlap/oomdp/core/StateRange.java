package burlap.oomdp.core;

/**
 * @author James MacGlashan.
 */
public interface StateRange extends State {


	/**
	 * Returns the numeric range of the variable for the given key.
	 * @param key the key of the variable
	 * @return a {@link VariableRange} specifying the range of the variable.
	 */
	VariableRange range(Object key);

}
