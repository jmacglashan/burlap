package burlap.oomdp.singleagent.explorer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


/**
 * This class allows you act as the agent by choosing actions to take in specific states. States are
 * conveyed to the user through a text description in the terminal and the user specifies actions
 * by typing the actions into the terminal. Shorthand names for actions names may be provided. Action
 * parameters are specified by space delineated input. For instance: "stack block0 block1" will cause
 * the stack action to called with action parameters block0 and block1. The command ##reset##
 * causes the state to reset to the initial state provided to the explorer.
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
				
				if(line.equals("##reset##")){
					s = src;
					this.lastAction = null;
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
	 * Prints the state s to the terminal.
	 * @param s the state to print to the terminal.
	 */
	public void printState(State s){
		
		System.out.println(s.getStateDescription());
		
	}
	

}
