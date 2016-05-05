package burlap.mdp.core.state;

import java.util.List;

/**
 * @author James MacGlashan.
 */
public class StateUtilities {
	public static String stateToString(State s){
		StringBuilder buf = new StringBuilder();
		buf.append("{\n");
		List<Object> keys = s.variableKeys();
		for(Object key : keys){
			buf.append(key.toString()).append(": {").append(s.get(key).toString()).append("}\n");
		}
		buf.append("}");
		return buf.toString();
	}
}
