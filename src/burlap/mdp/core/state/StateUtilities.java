package burlap.mdp.core.state;

import java.util.List;

/**
 * @author James MacGlashan.
 */
public class StateUtilities {


	public static Number stringOrNumber(Object o){
		if(o instanceof Number){
			return (Number)o;
		}
		else if(o instanceof String){
			Double d = Double.parseDouble((String)o);
			return d;
		}
		throw new RuntimeException("Value is neither a number nor a String.");
	}

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
