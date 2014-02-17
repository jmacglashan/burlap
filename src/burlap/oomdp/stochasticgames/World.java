package burlap.oomdp.stochasticgames;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.debugtools.DPrint;
import burlap.oomdp.auxiliary.StateAbstraction;
import burlap.oomdp.auxiliary.common.NullAbstraction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;


/**
 * This class provides a means to have agents play against each other and synchronize all of their actions and observations.
 * Any number of agents can join a World instance and they will be told when a game is starting, when a game ends, when
 * they need to provide an action, and what happened to all agents after every agent made their action selection. The world
 * may also make use of an option {@link burlap.oomdp.auxiliary.StateAbstraction} object so that agents are provided an 
 * abstract and simpler representation of the world. A game can be run until a terminal state is hit, or for a specific
 * number of stages, the latter of which is useful for repeated games.
 * @author James MacGlashan
 *
 */
public class World {

	protected SGDomain							domain;
	protected State								currentState;
	protected List <Agent>						agents;
	protected Map<AgentType, List<Agent>>		agentsByType;
	protected Map<String, Double>				agentCumulativeReward;
	
	protected JointActionModel 					worldModel;
	protected JointReward						jointRewardModel;
	protected TerminalFunction					tf;
	protected SGStateGenerator					initialStateGenerator;
	
	protected StateAbstraction					abstractionForAgents;
	
	
	protected JointAction						lastJointAction;
	
	
	protected List<WorldObserver>				worldObservers;
	
	protected int								debugId;
	
	
	
	/**
	 * Initializes the world.
	 * @param domain the SGDomain the world will use
	 * @param jam the joint action model that specifies the transition dynamics
	 * @param jr the joint reward function
	 * @param tf the terminal function
	 * @param sg a state generator for generating initial states of a game
	 */
	public World(SGDomain domain, JointActionModel jam, JointReward jr, TerminalFunction tf, SGStateGenerator sg){
		this.init(domain, jam, jr, tf, sg, new NullAbstraction());
	}
	
	/**
	 * Initializes the world
	 * @param domain the SGDomain the world will use
	 * @param jam the joint action model that specifies the transition dynamics
	 * @param jr the joint reward function
	 * @param tf the terminal function
	 * @param sg a state generator for generating initial states of a game
	 * @param abstractionForAgents the abstract state representation that agents will be provided
	 */
	public World(SGDomain domain, JointActionModel jam, JointReward jr, TerminalFunction tf, SGStateGenerator sg, StateAbstraction abstractionForAgents){
		this.init(domain, jam, jr, tf, sg, abstractionForAgents);
	}
	
	protected void init(SGDomain domain, JointActionModel jam, JointReward jr, TerminalFunction tf, SGStateGenerator sg, StateAbstraction abstractionForAgents){
		this.domain = domain;
		this.worldModel = jam;
		this.jointRewardModel = jr;
		this.tf = tf;
		this.initialStateGenerator = sg;
		this.abstractionForAgents = abstractionForAgents;
		
		agents = new ArrayList<Agent>();
		agentsByType = new HashMap<AgentType, List<Agent>>();
		
		agentCumulativeReward = new HashMap<String, Double>();
		
		worldObservers = new ArrayList<WorldObserver>();
		
		debugId = 284673923;
	}
	
	
	/**
	 * This class will report execution information as games are played using the {@link burlap.debugtools.DPrint} class. If the user
	 * wishes to suppress these messages, they can retrieve this code and suppress DPrint from printing messages that correspond to this code.
	 * @return the debug code used with {@link burlap.debugtools.DPrint}.
	 */
	public int getDebugId(){
		return debugId;
	}
	
	/**
	 * Sets the debug code that is use for printing with {@link burlap.debugtools.DPrint}.
	 * @param id the debug code to use when printing messages
	 */
	public void setDebugId(int id){
		debugId = id;
	}
	
	
	/**
	 * Returns the cumulative reward that the agent with name aname has received across all interactions in this world.
	 * @param aname the name of the agent
	 * @return the cumulative reward the agent has received in this world.
	 */
	public double getCumulativeRewardForAgent(String aname){
		return agentCumulativeReward.get(aname);
	}
	
	
	/**
	 * Registers an agent to be a participant in this world.
	 * @param a the agent to be registered in this world
	 * @param at the agent type the agent will be playing as
	 * @return the unique name that will identify this agent in this world.
	 */
	protected String registerAgent(Agent a, AgentType at){
		//don't register the same agent multiple times
		if(this.agentInstanceExists(a)){
			return a.worldAgentName;
		}
		
		String agentName = this.getNewWorldNameForAgentAndIndex(a, at);
		
		return agentName;
		
	}
	
	
	/**
	 * Returns the current world state
	 * @return the current world state
	 */
	public State getCurrentWorldState(){
		return this.currentState;
	}
	
	/**
	 * Causes the world to set the current state to a state generated by the provided {@link SGStateGenerator} object.
	 */
	public void generateNewCurrentState(){
		currentState = initialStateGenerator.generateState(agents);
	}
	
	/**
	 * Returns the last joint action taken in this world; null if none have been taken yet.
	 * @return the last joint action taken in this world; null if none have been taken yet
	 */
	public JointAction getLastJointAction(){
		return this.lastJointAction;
	}
	
	/**
	 * Adds a world observer to this world
	 * @param ob the observer to add
	 */
	public void addWorldObserver(WorldObserver ob){
		this.worldObservers.add(ob);
	}
	
	/**
	 * Removes the specified world observer from this world
	 * @param ob the world observer to remove
	 */
	public void removeWorldObserver(WorldObserver ob){
		this.worldObservers.remove(ob);
	}
	
	/**
	 * Clears all world observers from this world.
	 */
	public void clearAllWorldObserver(){
		this.worldObservers.clear();
	}
	
	
	/**
	 * Runs a game until a terminal state is hit.
	 */
	public void runGame(){
		
		for(Agent a : agents){
			a.gameStarting();
		}
		
		currentState = initialStateGenerator.generateState(agents);
		
		while(!tf.isTerminal(currentState)){
			this.runStage();
		}
		
		for(Agent a : agents){
			a.gameTerminated();
		}
		
		DPrint.cl(debugId, currentState.getCompleteStateDescription());
		
	}
	
	/**
	 * Runs a game until a terminal state is hit for maxStages have occurred
	 * @param maxStages the maximum number of stages to play in the game before its forced to end.
	 */
	public void runGame(int maxStages){
		
		for(Agent a : agents){
			a.gameStarting();
		}
		
		currentState = initialStateGenerator.generateState(agents);
		int t = 0;
		
		while(!tf.isTerminal(currentState) && t < maxStages){
			this.runStage();
			t++;
		}
		
		for(Agent a : agents){
			a.gameTerminated();
		}
		
		DPrint.cl(debugId, currentState.getCompleteStateDescription());
		
	}
	
	/**
	 * Runs a single stage of this game.
	 */
	public void runStage(){
		if(tf.isTerminal(currentState)){
			return ; //cannot continue this game
		}
		
		
		
		JointAction ja = new JointAction(agents.size());
		State abstractedCurrent = abstractionForAgents.abstraction(currentState);
		for(Agent a : agents){
			ja.addAction(a.getAction(abstractedCurrent));
		}
		this.lastJointAction = ja;
		
		
		DPrint.cl(debugId, ja.toString());
		
		
		//now that we have the joint action, perform it
		State sp = worldModel.performJointAction(currentState, ja);
		State abstractedPrime = this.abstractionForAgents.abstraction(sp);
		Map<String, Double> jointReward = jointRewardModel.reward(currentState, ja, sp);
		
		DPrint.cl(debugId, jointReward.toString());
		
		//index reward
		for(String aname : jointReward.keySet()){
			double curCumR = agentCumulativeReward.get(aname);
			curCumR += jointReward.get(aname);
			agentCumulativeReward.put(aname, curCumR);
		}
		
		//tell all the agents about it
		for(Agent a : agents){
			a.observeOutcome(abstractedCurrent, ja, jointReward, abstractedPrime, tf.isTerminal(sp));
		}
		
		//tell observers
		for(WorldObserver o : this.worldObservers){
			o.observe(currentState, ja, jointReward, sp);
		}
		
		//update the state
		currentState = sp;
		
	}
	
	/**
	 * Returns the {@link JointActionModel} used in this world.
	 * @return the {@link JointActionModel} used in this world.
	 */
	public JointActionModel getActionModel(){
		return worldModel;
	}
	
	
	/**
	 * Returns the {@link JointReward} function used in this world.
	 * @return the {@link JointReward} function used in this world.
	 */
	public JointReward getRewardModel(){
		return jointRewardModel;
	}
	
	/**
	 * Returns the {@link burlap.oomdp.core.TerminalFunction} used in this world.
	 * @return the {@link burlap.oomdp.core.TerminalFunction} used in this world.
	 */
	public TerminalFunction getTF(){
		return tf;
	}
	
	
	/**
	 * Returns the list of agents participating in this world.
	 * @return the list of agents participating in this world.
	 */
	public List <Agent> getRegisteredAgents(){
		return new ArrayList<Agent>(agents);
	}
	
	
	/**
	 * Returns the player index for the agent with the given name.
	 * @param aname the name of the agent
	 * @return the player index of the agent with the given name.
	 */
	public int getPlayerNumberForAgent(String aname){
		for(int i = 0; i < agents.size(); i++){
			Agent a = agents.get(i);
			if(a.worldAgentName.equals(aname)){
				return i;
			}
		}
		
		return -1;
	}
	
	
	protected String getNewWorldNameForAgentAndIndex(Agent a, AgentType type){
	
		
		List <Agent> aots = agentsByType.get(type);
		if(aots == null){
			aots = new ArrayList<Agent>();
			agentsByType.put(type, aots);
		}
		
		String name = type.typeName + aots.size();
		agents.add(a);
		aots.add(a);
		
		agentCumulativeReward.put(name, 0.);
		
		return name;
	}
	
	protected boolean agentInstanceExists(Agent a){
		for(Agent A : agents){
			if(A == a){
				return true;
			}
		}
		
		return false;
	}

}
