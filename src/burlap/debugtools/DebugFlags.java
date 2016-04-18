package burlap.debugtools;

import java.util.HashMap;
import java.util.Map;


/**
 * A data structure for specifying debug flags that can be accessed and modified from any class
 * @author James MacGlashan
 *
 */
public class DebugFlags {

	/**
	 * The flags and their values that are set
	 */
	private static Map <Integer, Integer> flags;
	
	private DebugFlags() {
	    // do nothing
	}
	
	/**
	 * Creates/sets a debug flag
	 * @param id the flag identifier
	 * @param v the value of the flag
	 */
	public static void setFlag(int id, int v){
		if(flags == null){
			flags = new HashMap <Integer, Integer>();
		}
		flags.put(id, v);
	}
	
	
	/**
	 * Returns the value for a given flag; 0 if the flag has never been created/set
	 * @param id the flag identifier
	 * @return the value of the flag; 0 if the flag has never been created/set
	 */
	public static int getFlag(int id){
		if(flags == null){
			flags = new HashMap <Integer, Integer>();
		}
		Integer v = flags.get(id);
		if(v == null){
			flags.put(id, 0);
			return 0;
		}
		return v;
	}
	
	
}
