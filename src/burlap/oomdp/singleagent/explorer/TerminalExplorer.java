package burlap.oomdp.singleagent.explorer;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This class allows you act as the agent by choosing actions to take in specific states. States are
 * conveyed to the user through a text description in the terminal and the user specifies actions
 * by typing the actions into the terminal. Shorthand names for actions names may be provided. Action
 * parameters are specified by space delineated input. For instance: "stack block0 block1" will cause
 * the stack action to called with action parameters block0 and block1. The command #reset
 * causes the state to reset to the initial state provided to the explorer. Other special commands
 * to modify the state can also be given by starting the line with a # symbol. The full syntax structure for modifying
 * states will be printed to the terminal when the explorer is launched.
 * <br/><br/>
 * This class can also be provided a reward function and terminal function through the
 * {@link #setRewardFunction(burlap.oomdp.singleagent.RewardFunction)} and
 * {@link #setTerminalFunctionf(burlap.oomdp.core.TerminalFunction)} methods, which will
 * cause the terminal to print out the reward for transitions and whether the current state
 * is a terminal state.
 * @author James MacGlashan
 *
 */
public class TerminalExplorer {
	
	protected Domain					domain;
	protected Map <String, String>		actionShortHand;
	protected RewardFunction			rewardFunction;
	protected TerminalFunction			terminalFunction;


	protected GroundedAction			lastAction;
	
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


	public RewardFunction getRewardFunction() {
		return rewardFunction;
	}

	public void setRewardFunction(RewardFunction rewardFunction) {
		this.rewardFunction = rewardFunction;
	}

	public TerminalFunction getTerminalFunction() {
		return terminalFunction;
	}

	public void setTerminalFunctionf(TerminalFunction terminalFunction) {
		this.terminalFunction = terminalFunction;
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


		System.out.println("Special Command Syntax:\n"+
				"    #add objectClass object\n" +
				"    #remove object\n" +
				"    #set object attribute [attribute_2 ... attribute_n] value [value_2 ... value_n]\n" +
				"    #addRelation sourceObject relationalAttribute targetObject\n" +
				"    #removeRelation sourceObject relationalAttribute targetObject\n" +
				"    #clearRelations sourceObject relationalAttribute\n" +
				"    #reset\n\n");


		State src = s.copy();
		State oldState = src;
		String actionPromptDelimiter = "-----------------------------------";
		
		while(true){
			
			this.printState(s);
			if(this.terminalFunction != null){
				if(this.terminalFunction.isTerminal(s)){
					System.out.println("State IS terminal");
				}
				else{
					System.out.println("State is NOT terminal");
				}
			}
			if(this.rewardFunction != null && this.lastAction != null){
				double r = this.rewardFunction.reward(oldState, lastAction, s);
				System.out.println("Reward: " + r);
			}
			
			System.out.println(actionPromptDelimiter);
			
			BufferedReader in;
			String line;
			try{
			
				in = new BufferedReader(new InputStreamReader(System.in));
				line = in.readLine();
				
				if(line.equals("#reset")){
					s = src;
					this.lastAction = null;
				}
				else if(line.startsWith("#")){
					//then do console command parsing
					String command = line.substring(1).trim();
					State ns = this.parseCommand(s, command);
					if(ns != null) {
						this.lastAction = null;
						s = ns;
					}
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
						System.out.println("Unknown action: " + actionName + "; nothing changed");
					}
					else{
						GroundedAction ga = new GroundedAction(action, params);
						if(action.applicableInState(s, params)) {
							oldState = s;
							s = action.performAction(s, params);
							this.lastAction = ga;
						}
						else{
							System.out.println(ga.toString() + " is not applicable in the current state; nothing changed");
						}
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
	 * Parses a command and returns the resulted modified state
	 * @param curState the current state to modify
	 * @param command the special command to parse
	 * @return the modified state
	 */
	protected State parseCommand(State curState, String command){
		String [] comps = command.split(" ");
		State ns = curState.copy();
		if(comps.length > 0) {


			if(comps[0].equals("set")) {
				if(comps.length >= 4) {
					ObjectInstance o = ns.getObject(comps[1]);
					if(o != null) {
						int rsize = comps.length - 2;
						if(rsize % 2 == 0) {
							int vind = rsize / 2;
							for(int i = 0; i < rsize / 2; i++) {
								o.setValue(comps[2 + i], comps[2 + i + vind]);
							}
						}
					}
				}

			} else if(comps[0].equals("addRelation")) {
				if(comps.length == 4) {
					ObjectInstance o = ns.getObject(comps[1]);
					if(o != null) {
						o.addRelationalTarget(comps[2], comps[3]);
					}
				}
			} else if(comps[0].equals("removeRelation")) {
				if(comps.length == 4) {
					ObjectInstance o = ns.getObject(comps[1]);
					if(o != null) {
						o.removeRelationalTarget(comps[2], comps[3]);
					}
				}
			} else if(comps[0].equals("clearRelations")) {
				if(comps.length == 3) {
					ObjectInstance o = ns.getObject(comps[1]);
					if(o != null) {
						o.clearRelationalTargets(comps[2]);
					}
				}
			} else if(comps[0].equals("add")) {
				if(comps.length == 3) {
					ObjectInstance o = new MutableObjectInstance(this.domain.getObjectClass(comps[1]), comps[2]);
					ns.addObject(o);
				}
			} else if(comps[0].equals("remove")) {
				if(comps.length == 2) {
					ns.removeObject(comps[1]);
				}
			}
		}

		return ns;

	}
	
	
	/**
	 * Prints the state s to the terminal.
	 * @param s the state to print to the terminal.
	 */
	public void printState(State s){
		
		System.out.println(s.getCompleteStateDescriptionWithUnsetAttributesAsNull());
		
	}


	

}
