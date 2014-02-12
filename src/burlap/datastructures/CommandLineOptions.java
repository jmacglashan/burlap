package burlap.datastructures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This class is used to extract command line options that are specified in the form: "--option" or "--option=vaue". If an option does not have
 * a value, its associated value will be the empty string. If an option is not set, the returned value is the empty string. Any command line
 * arguments that are not the specified form are not included in the option set.
 * @author James MacGlashan
 *
 */
public class CommandLineOptions {

	protected Map<String, String> optionValue;
	
	
	/**
	 * Parses an array of command line arguments for the options and their values
	 * @param args the command line arguments passed to a main method.
	 */
	public CommandLineOptions(String [] args){
		this.optionValue = new HashMap<String, String>(args.length);
		for(String arg : args){
			if(arg.substring(0, 2).equals("--")){
				//then this is an option
				String noPrefix = arg.substring(2);
				String [] elements = noPrefix.split("=");
				if(elements.length == 1){
					optionValue.put(elements[0], "");
				}
				else{
					optionValue.put(elements[0], elements[1]);
				}
			}
		}
	}
	
	/**
	 * Returns whether the queried opton is set.
	 * This method will automatically ignore a prefix "--" if it is included in the option name.
	 * @param option the option to check for.
	 * @return true if the option was set; false otherwise
	 */
	public boolean containsOption(String option){
		if(option.startsWith("--")){
			option = option.substring(2);
		}
		return this.optionValue.containsKey(option);
	}
	
	
	/**
	 * Returns the value of the queried option.
	 * If the the queried option is not set or has not associated value, then the empty string is returned.
	 * This method will automatically ignore a prefix "--" if it is included in the option name.
	 * @param option the option to get the value for.
	 * @return the empty string if the option is not set or has no associated value; the associated value otherwise.
	 */
	public String optionValue(String option){
		if(option.startsWith("--")){
			option = option.substring(2);
		}
		String result = this.optionValue.get(option);
		result = result != null ? result : "";
		return result;
	}
	
	
	/**
	 * Returns the list of options whose name starts with the given option prefix.
	 * This method will automatically ignore a prefix "--" if it is included in the optionPrefix name.
	 * @param optionPrefix the option prefix that the returned options will contain
	 * @return the list of options that start with the specified option prefix
	 */
	public List<String> getOptionsStartingWithName(String optionPrefix){
		if(optionPrefix.startsWith("--")){
			optionPrefix = optionPrefix.substring(2);
		}
		
		List<String> options = new ArrayList<String>();
		for(String option : this.optionValue.keySet()){
			if(option.startsWith(optionPrefix)){
				options.add(option);
			}
		}
			
		return options;
	}
	
	
}
