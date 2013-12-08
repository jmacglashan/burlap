package burlap.debugtools;

import java.util.HashMap;
import java.util.Map;


/**
 * A class for managing debug print statements. Different debug print statements can be associated with different debug ids and enabling or disabling
 * print commands for that debug id can be performed from any class
 * @author James MacGlashan
 *
 */
public class DPrint {
	/**
	 * data structure keeping track of which debugCodes are allowed to print
	 */
	static Map<Integer, Boolean> 	debugCodes = new HashMap<Integer, Boolean>();
	
	/**
	 * Boolean which indicates that previously unset debug codes will by default be allowed to print
	 * Setting this value to false will cause all otherwise unspecified debug values to not print
	 */
	static boolean					universalPrint = true;
	
	
	/**
	 * Specify whether previously unset debug codes will by default be allowed to print or not.
	 * @param mode whether to enable default printing or default suppression.
	 */
	public static void toggleUniversal(boolean mode){
		universalPrint = mode;
	}
	
	
	/**
	 * Enables/disables print commands to the given debug code
	 * @param c the print debug code to set
	 * @param mode true indicates that print calls to that code will print; false means that they will not
	 */
	public static void toggleCode(int c, boolean mode){
		debugCodes.put(c, mode);
	}
	
	
	/**
	 * A universal print line whose behavior is determined by the {@link universalPrint} field
	 * @param s the string to print
	 */
	public static void ul(String s){
		u(s+"\n");
	}
	
	
	/**
	 * A universal print whose behavior is determined by the {@link universalPrint} field
	 * @param s the string to print
	 */
	public static void u(String s){
		if(universalPrint){
			System.out.print(s);
		}
	}
	
	
	/**
	 * A universal printf whose behavior is determined by the {@link universalPrint} field
	 * @param s the format string
	 * @param args the arguments for the formatted string
	 */
	public static void uf(String s, Object...args){
		if(universalPrint){
			System.out.printf(s, args);
		}
	}
	
	/**
	 * A print line command for the given debug code. If that debug code is set to false, then the print will not occur.
	 * @param c the debug code under which printing should be performed
	 * @param s the string to print
	 */
	public static void cl(int c, String s){
		c(c, s+"\n");
	}
	
	
	/**
	 * A print command for the given debug code. If that debug code is set to false, then the print will not occur.
	 * @param c the debug code under which printing should be performed
	 * @param s the string to print
	 */
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
	
	
	
	/**
	 * A printf command for the given debug code. If that debug code is set to false, then the print will not occur.
	 * @param c the debug code under which printing should be performed
	 * @param s the format string
	 * @param args the arguments for the formatted string
	 */
	public static void cf(int c, String s, Object...args){
		Boolean m = debugCodes.get(c);
		boolean mb = universalPrint;
		if(m != null){
			mb = m;
		}
		if(mb){
			System.out.printf(s,args);
		}
	}
	
	
	/**
	 * Returns the print mode for a given debug code
	 * @param c the code to query.
	 * @return true if printing to that debug code is allowed; false if it is not allowed.
	 */
	public static boolean mode(int c){
		Boolean m = debugCodes.get(c);
		boolean mb = universalPrint;
		if(m != null){
			mb = m;
		}
		return mb;
	}

}
