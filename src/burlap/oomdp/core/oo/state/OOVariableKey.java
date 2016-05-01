package burlap.oomdp.core.oo.state;

/**
 * @author James MacGlashan.
 */
public class OOVariableKey {
	public String obName;
	public Object obVarKey;


	public OOVariableKey() {
	}

	public OOVariableKey(String strForm){
		int ind = strForm.indexOf(':');
		if(ind == -1){
			throw new RuntimeException("Cannot parse string rep of OOVariableKey, because it does not have a : separating object name and object key");
		}
		this.obName = strForm.substring(0, ind);
		this.obVarKey = strForm.substring(ind+1);
	}

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
