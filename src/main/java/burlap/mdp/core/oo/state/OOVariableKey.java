package burlap.mdp.core.oo.state;

/**
 * A tuple for referring to the variable of a specific {@link OOState} object.
 * @author James MacGlashan.
 */
public class OOVariableKey {

	/**
	 * The name of the {@link OOState} object.
	 */
	public String obName;

	/**
	 * The variable key of the {@link OOState} object.
	 */
	public Object obVarKey;


	public OOVariableKey() {
	}


	/**
	 * Initializes when the key is specified in a string form. That is, a string with the format object_name:variable_key
	 * @param strForm the string form of the key
	 */
	public OOVariableKey(String strForm){
		int ind = strForm.indexOf(':');
		if(ind == -1){
			throw new RuntimeException("Cannot parse string rep of OOVariableKey, because it does not have a : separating object name and object key");
		}
		this.obName = strForm.substring(0, ind);
		this.obVarKey = strForm.substring(ind+1);
	}

	/**
	 * Initializes
	 * @param obName the name of the {@link OOState} object
	 * @param obVarKey the variable key of the {@link OOState} object.
	 */
	public OOVariableKey(String obName, Object obVarKey) {
		this.obName = obName;
		this.obVarKey = obVarKey;
	}

	@Override
	public String toString() {
		return obName + ":" + obVarKey.toString();
	}

	@Override
	public int hashCode() {
		return obName.hashCode() + 31 * obVarKey.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
}
