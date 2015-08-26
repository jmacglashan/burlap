package burlap.oomdp.stochasticgames.explorers;

import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.stochasticgames.*;
import burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction;
import burlap.oomdp.stochasticgames.agentactions.SGAgentAction;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * This class allows you act as all of the agents in a domain by choosing actions for each of them to take in specific states. States are
 * conveyed to the user through a text description in the terminal and the user specifies actions
 * by typing the actions into the terminal, one line at a time for each agent's action. 
 * The command format is "agentName:action parameter1 parameter2" and so on for as many parameters
 * as there may be (or none if an action takes no parameters). 
 * <p/>
 * When all of the agent's actions have
 * been input, they may be executed by typing "##"
 * <p/>
 * Shorthand names for actions names may be provided. 
 * <p/>
 * The command #reset
 * causes the state to reset to the initial state provided to the explorer. Other special commands to modify
 * the state may also be specified. The full syntax is printed to the terminal when the explorer begins.
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
		List <SGAgentAction> actionList = domain.getAgentActions();
		for(SGAgentAction a : actionList){
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

		System.out.println("Special Command Syntax:\n"+
				"    #add objectClass object\n" +
				"    #remove object\n" +
				"    #set object attribute [attribute_2 ... attribute_n] value [value_2 ... value_n]\n" +
				"    #addRelation sourceObject relationalAttribute targetObject\n" +
				"    #removeRelation sourceObject relationalAttribute targetObject\n" +
				"    #clearRelations sourceObject relationalAttribute\n" +
				"    #reset\n\n");
		System.out.println("Set agent actions with the notation \"agentName:action actionParam1 actionParam2 ...\"");
		System.out.println("Type \"##\" to commit and execute the current joint action.\n\n");


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
				
				if(line.equals("#reset")){
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
				else if(line.startsWith("#")){

					//then do console command parsing
					String command = line.substring(1).trim();
					State ns = this.parseCommand(s, command);
					if(ns != null) {
						this.curJointAction = new JointAction();
						s = ns;

						this.printState(ns);

						if(this.terminalFunction != null){
							if(this.terminalFunction.isTerminal(ns)){
								System.out.println("State IS terminal");
							}
							else{
								System.out.println("State is NOT terminal");
							}
						}

						System.out.println(actionPromptDelimiter);
					}
				}
				else{
					
					//split the string up into components
					String [] agacComps = line.split(":");
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
					
					SGAgentAction sa = this.domain.getSingleAction(actionName);
					if(sa == null){
						System.out.println("Unknown action: " + actionName);
					}
					else{
						GroundedSGAgentAction gsa = sa.getAssociatedGroundedAction(agentName);
						gsa.initParamsWithStringRep(params);
						if(sa.applicableInState(s, gsa)){
							System.out.println("Setting action: " + agentName + "::" + actionName);
							curJointAction.addAction(gsa);
						}
						else{
							System.out.println(gsa.toString() + " is not applicable in the current state; nothing changed");
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
	 * Prints the given state to the terminal.
	 * @param s the state to print to the terminal.
	 */
	public void printState(State s){
		
		System.out.println(s.getCompleteStateDescriptionWithUnsetAttributesAsNull());
		
	}



}
