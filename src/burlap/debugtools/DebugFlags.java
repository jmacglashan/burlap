package burlap.debugtools;

import java.util.HashMap;
import java.util.Map;

public class DebugFlags {

	private static Map <Integer, Integer> flags;
	
	
	public static void setFlag(int id, int v){
		if(flags == null){
			flags = new HashMap <Integer, Integer>();
		}
		flags.put(id, v);
	}
	
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
