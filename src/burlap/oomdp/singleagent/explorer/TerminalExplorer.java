package burlap.oomdp.singleagent.explorer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;



/**
 * This class allows you act as the agent by choosing actions to take in specific states. States are
 * conveyed to the user through a text description in the terminal and the user specifies actions
 * by typing the actions into the terminal. Shorthand names for actions names may be provided. Action
 * parameters are specified by space delineated input. For instance: "stack block0 block1" will cause
 * the stack action to called with action parameters block0 and block1. The command ##reset##
 * causes the state to reset to the initial state provided to the explorer.
 * @author James MacGlashan
 *
 */
public class TerminalExplorer {
	
	private Domain					domain;
	private Map <String, String>	actionShortHand;
	
	
	/**
	 * Initializes the explorer with the specified domain
	 * @param domain the domain to explore
	 */
	public TerminalExplorer(Domain domain){
		this.domain = domain;
		this.setActionShortHand(new HashMap <String, String>());
	}
	
	/**
	 * Initializes the explorer with the specified domain and short hand names for actions
	 * @param domain the domain to explore
	 * @param ash a map from short hand names to full action names. For instance, "s->stack"
	 */
	public TerminalExplorer(Domain domain, Map <String, String> ash){
		this.domain = domain;
		this.setActionShortHand(ash);
	}
	
	/**
	 * Sets teh short hand names to use for actions.
	 * @param ash a map from short hand names to full action names. For instance, "s->stack"
	 */
	public void setActionShortHand(Map <String, String> ash){
		this.actionShortHand = ash;
		List <Action> actionList = domain.getActions();
		for(Action a : actionList){
			this.addActionShortHand(a.getName(), a.getName());
		}
	}
	
	
	/**
	 * Adds a short hand name for an action name
	 * @param shortHand the short hand name to use
	 * @param action the full action name
	 */
	public void addActionShortHand(String shortHand, String action){
		actionShortHand.put(shortHand, action);
	}
	
	
	/**
	 * Starts the explorer to run from state s
	 * @param s the state from which to explore.
	 */
	public void exploreFromState(State s){
		
		State src = s.copy();
		String actionPromptDelimiter = "-----------------------------------";
		
		while(true){
			
			this.printState(s);
			
			System.out.println(actionPromptDelimiter);
			
			BufferedReader in;
			String line;
			try{
			
				in = new BufferedReader(new InputStreamReader(System.in));
				line = in.readLine();
				
				if(line.equals("##reset##")){
					s = src;
				}
				else{
					
					//split the string up into components
					String [] comps = line.split(" ");
					String actionName = actionShortHand.get(comps[0]);
					
					if(actionName == null){
						actionName = comps[0];
					}
					
					//construct parameter list as all that remains
					String params[];
					if(comps.length > 1){
						params = new String[comps.length-1];
						for(int i = 1; i < comps.length; i++){
							params[i-1] = comps[i];
						}
					}
					else{
						params = new String[0];
					}
					
					Action action = domain.getAction(actionName);
					if(action == null){
						System.out.println("Unknown action: " + actionName);
					}
					else{
						s = action.performAction(s, params);
					}
					
				}
				
				System.out.println(actionPromptDelimiter);
				
			}
				
			catch(Exception e){
				System.out.println(e);
			}
			
		}
		
		
	}
	
	
	/**
	 * Prints the state s to the terminal.
	 * @param s the state to print to the terminal.
	 */
	public void printState(State s){
		
		System.out.println(s.getStateDescription());
		
	}
	

}
