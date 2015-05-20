package burlap.oomdp.stochasticgames.explorers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.stochasticgames.GroundedSingleAction;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.JointActionModel;
import burlap.oomdp.stochasticgames.JointReward;
import burlap.oomdp.stochasticgames.SGDomain;
import burlap.oomdp.stochasticgames.SingleAction;



/**
 * This class allows you act as all of the agents in a domain by choosing actions for each of them to take in specific states. States are
 * conveyed to the user through a text description in the terminal and the user specifies actions
 * by typing the actions into the terminal, one line at a time for each agent's action. 
 * The command format is "agentName::action parameter1 parameter2" and so on for as many parameters
 * as there may be (or none if an action takes no parameters). 
 * <p/>
 * When all of the agent's actions have
 * been input, they may be executed by typing "##"
 * <p/>
 * Shorthand names for actions names may be provided. 
 * <p/>
 * The command ##reset##
 * causes the state to reset to the initial state provided to the explorer.
 * <br/><br/>
 * This explorer can also track a reward function and terminal function and print them to the screen, which can be set
 * with the
 * {@link #setRewardFunction(burlap.oomdp.stochasticgames.JointReward)} and
 * {@link #setTerminalFunction(burlap.oomdp.core.TerminalFunction)} methods.
 * @author James MacGlashan
 *
 */
public class SGTerminalExplorer {

	protected SGDomain					domain;
	protected Map <String, String>		actionShortHand;
	protected JointActionModel			jam;
	protected JointAction				curJointAction;
	protected JointReward				rewardFunction;
	protected TerminalFunction			terminalFunction;
	
	
	/**
	 * This constructor is deprecated, because {@link burlap.oomdp.stochasticgames.SGDomain} objects are now expected
	 * to have a {@link burlap.oomdp.stochasticgames.JointActionModel} associated with them, making the constructor parameter for it
	 * unnecessary. Instead use the constructor {@link #SGTerminalExplorer(burlap.oomdp.stochasticgames.SGDomain)}.
	 * @param domain the domain which will be explored
	 * @param jam the action model definition transition dynamics
	 */
	@Deprecated
	public SGTerminalExplorer(SGDomain domain, JointActionModel jam){
		this.domain = domain;
		this.jam = jam;
		this.setActionShortHand(new HashMap <String, String>());
		this.rewardFunction = null;
	}

	/**
	 * Initializes the explorer with a domain and action model
	 * @param domain the domain which will be explored
	 */
	public SGTerminalExplorer(SGDomain domain){
		this.domain = domain;
		this.jam = domain.getJointActionModel();
		this.setActionShortHand(new HashMap <String, String>());
		this.rewardFunction = null;
	}
	
	/**
	 *This constructor is deprecated, because {@link burlap.oomdp.stochasticgames.SGDomain} objects are now expected
	 * to have a {@link burlap.oomdp.stochasticgames.JointActionModel} associated with them, making the constructor parameter for it
	 * unnecessary. Instead use the constructor {@link #SGTerminalExplorer(burlap.oomdp.stochasticgames.SGDomain, java.util.Map)}.
	 * @param domain the domain which will be explored
	 * @param jam the action model definition transition dynamics
	 * @param ash a map from shorthand names to full action names that can be typed instead of the full action names
	 */
	@Deprecated
	public SGTerminalExplorer(SGDomain domain, JointActionModel jam, Map <String, String> ash){
		this.domain = domain;
		this.jam = jam;
		this.setActionShortHand(ash);
		this.rewardFunction = null;
	}

	/**
	 * Initializes the explorer with a domain and action model and shorthand names for actions
	 * @param domain the domain which will be explored
	 * @param ash a map from shorthand names to full action names that can be typed instead of the full action names
	 */
	public SGTerminalExplorer(SGDomain domain, Map <String, String> ash){
		this.domain = domain;
		this.jam = domain.getJointActionModel();
		this.setActionShortHand(ash);
		this.rewardFunction = null;
	}
	
	
	/**
	 * Allows the explorer to keep track of the reward received that will be printed to the output.
	 * @param rf the reward function to use.
	 */
	public void setRewardFunction(JointReward rf){
		this.rewardFunction = rf;
	}

	public JointReward getRewardFunction() {
		return rewardFunction;
	}

	public TerminalFunction getTerminalFunction() {
		return terminalFunction;
	}

	public void setTerminalFunction(TerminalFunction terminalFunction) {
		this.terminalFunction = terminalFunction;
	}

	/**
	 * Sets the action shorthands to use
	 * @param ash a map from action shorthands to full action names
	 */
	public void setActionShortHand(Map <String, String> ash){
		this.actionShortHand = ash;
		List <SingleAction> actionList = domain.getSingleActions();
		for(SingleAction a : actionList){
			this.addActionShortHand(a.actionName, a.actionName);
		}
	}
	
	
	/**
	 * Adds a shorthand for an action
	 * @param shortHand the shorthand for the action
	 * @param action the full name of the action that the shorthand represents
	 */
	public void addActionShortHand(String shortHand, String action){
		actionShortHand.put(shortHand, action);
	}
	
	
	/**
	 * Causes the explorer to begin from the given input state
	 * @param s the state from which to start exploring.
	 */
	public void exploreFromState(State s){
		
		curJointAction = new JointAction();
		
		State src = s.copy();
		String actionPromptDelimiter = "-----------------------------------";
		
		this.printState(s);
		if(this.terminalFunction != null){
			if(this.terminalFunction.isTerminal(s)){
				System.out.println("State IS terminal");
			}
			else{
				System.out.println("State is NOT terminal");
			}
		}
		System.out.println(actionPromptDelimiter);
		
		while(true){
			
			
			BufferedReader in;
			String line;
			try{
			
				in = new BufferedReader(new InputStreamReader(System.in));
				line = in.readLine();
				
				if(line.equals("##reset##")){
					s = src;
					curJointAction = new JointAction();
					this.printState(s);

					if(this.terminalFunction != null){
						if(this.terminalFunction.isTerminal(s)){
							System.out.println("State IS terminal");
						}
						else{
							System.out.println("State is NOT terminal");
						}
					}

					System.out.println(actionPromptDelimiter);
				}
				else if(line.equals("##")){
					State ns = this.jam.performJointAction(s, curJointAction);
					

					this.printState(ns);

					if(this.terminalFunction != null){
						if(this.terminalFunction.isTerminal(ns)){
							System.out.println("State IS terminal");
						}
						else{
							System.out.println("State is NOT terminal");
						}
					}

					if(this.rewardFunction != null){
						Map<String, Double> reward = rewardFunction.reward(s, curJointAction, ns);
						for(String aname : reward.keySet()){
							System.out.println("" + aname + ": " + reward.get(aname));
						}
					}

					System.out.println(actionPromptDelimiter);

					curJointAction = new JointAction();
					s = ns;
				}
				else{
					
					//split the string up into components
					String [] agacComps = line.split("::"); 
					String agentName = agacComps[0];
					
					String [] comps = agacComps[1].split(" ");
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
					
					SingleAction sa = this.domain.getSingleAction(actionName);
					if(sa == null){
						System.out.println("Unknown action: " + actionName);
					}
					else{
						GroundedSingleAction gsa = new GroundedSingleAction(agentName, sa, params);
						if(sa.isApplicableInState(s, agentName, params)){
							System.out.println("Setting action: " + agentName + "::" + actionName);
							curJointAction.addAction(gsa);
						}
						else{
							System.out.println("Cannot apply this action in this state: " + agentName + "::" + actionName);
						}
					}
					

					
				}
				
				
			}
				
			catch(Exception e){
				System.out.println(e);
			}
			
		}
		
		
	}
	
	
	/**
	 * Prints the given state to the terminal.
	 * @param s the state to print to the terminal.
	 */
	public void printState(State s){
		
		System.out.println(s.getStateDescription());
		
	}

}
