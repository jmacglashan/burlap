package burlap.debugtools;

import java.util.HashMap;
import java.util.Map;

public class DPrint {
	
	static Map<Integer, Boolean> 	debugCodes = new HashMap<Integer, Boolean>();
	static boolean					universalPrint = true;
	
	
	public static void toggleUniversal(boolean mode){
		universalPrint = mode;
	}
	
	public static void toggleCode(int c, boolean mode){
		debugCodes.put(c, mode);
	}
	
	public static void ul(String s){
		u(s+"\n");
	}
	
	public static void u(String s){
		if(universalPrint){
			System.out.print(s);
		}
	}
	
	public static void cl(int c, String s){
		c(c, s+"\n");
	}
	
	public static void c(int c, String s){
		Boolean m = debugCodes.get(c);
		boolean mb = universalPrint;
		if(m != null){
			mb = m;
		}
		if(mb){
			System.out.print(s);
		}
	}
	
	public static boolean mode(int c){
		Boolean m = debugCodes.get(c);
		boolean mb = universalPrint;
		if(m != null){
			mb = m;
		}
		return mb;
	}

}
