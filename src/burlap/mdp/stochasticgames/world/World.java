package burlap.mdp.stochasticgames.world;

import burlap.behavior.stochasticgames.GameAnalysis;
import burlap.behavior.stochasticgames.JointPolicy;
import burlap.datastructures.HashedAggregator;
import burlap.debugtools.DPrint;
import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.auxiliary.common.IdentityStateMapping;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.action.JointAction;
import burlap.mdp.stochasticgames.SGDomain;
import burlap.mdp.stochasticgames.SGStateGenerator;
import burlap.mdp.stochasticgames.agent.SGAgent;
import burlap.mdp.stochasticgames.agent.SGAgentType;
import burlap.mdp.stochasticgames.common.ConstantSGStateGenerator;
import burlap.mdp.stochasticgames.model.JointActionModel;
import burlap.mdp.stochasticgames.model.JointRewardFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


/**
 * This class provides a means to have agents play against each other and synchronize all of their actions and observations.
 * Any number of agents can join a World instance and they will be told when a game is starting, when a game ends, when
 * they need to provide an action, and what happened to all agents after every agent made their action selection. The world
 * may also make use of an optional {@link burlap.mdp.auxiliary.StateMapping} object so that agents are provided an
 * abstract and simpler representation of the world. A game can be run until a terminal state is hit, or for a specific
 * number of stages, the latter of which is useful for repeated games.
 * @author James MacGlashan
 *
 */
public class World {

	protected SGDomain domain;
	protected State								currentState;
	protected List <SGAgent>						agents;
	protected Map<SGAgentType, List<SGAgent>>		agentsByType;
	protected HashedAggregator<String>			agentCumulativeReward;
	protected Map<String, SGAgentType>			agentDefinitions;
	
	protected JointActionModel worldModel;
	protected JointRewardFunction jointRewardFunction;
	protected TerminalFunction					tf;
	protected SGStateGenerator initialStateGenerator;
	
	protected StateMapping					abstractionForAgents;
	
	
	protected JointAction lastJointAction;
	
	
	protected List<WorldObserver>				worldObservers;
	
	
	protected GameAnalysis						currentGameRecord;
	protected boolean							isRecordingGame = false;
	
	protected int								debugId;

	protected Map<String, Double>				lastRewards = new HashMap<String, Double>();




	/**
	 * Initializes the world.
	 * @param domain the SGDomain the world will use
	 * @param jr the joint reward function
	 * @param tf the terminal function
	 * @param initialState the initial state of the world every time a new game starts
	 */
	public World(SGDomain domain, JointRewardFunction jr, TerminalFunction tf, State initialState){
		this.init(domain, domain.getJointActionModel(), jr, tf, new ConstantSGStateGenerator(initialState), new IdentityStateMapping());
	}


	/**
	 * Initializes the world.
	 * @param domain the SGDomain the world will use
	 * @param jr the joint reward function
	 * @param tf the terminal function
	 * @param sg a state generator for generating initial states of a game
	 */
	public World(SGDomain domain, JointRewardFunction jr, TerminalFunction tf, SGStateGenerator sg){
		this.init(domain, domain.getJointActionModel(), jr, tf, sg, new IdentityStateMapping());
	}


	/**
	 * Initializes the world
	 * @param domain the SGDomain the world will use
	 * @param jr the joint reward function
	 * @param tf the terminal function
	 * @param sg a state generator for generating initial states of a game
	 * @param abstractionForAgents the abstract state representation that agents will be provided
	 */
	public World(SGDomain domain, JointRewardFunction jr, TerminalFunction tf, SGStateGenerator sg, StateMapping abstractionForAgents){
		this.init(domain, domain.getJointActionModel(), jr, tf, sg, abstractionForAgents);
	}
	
	protected void init(SGDomain domain, JointActionModel jam, JointRewardFunction jr, TerminalFunction tf, SGStateGenerator sg, StateMapping abstractionForAgents){
		this.domain = domain;
		this.worldModel = jam;
		this.jointRewardFunction = jr;
		this.tf = tf;
		this.initialStateGenerator = sg;
		this.abstractionForAgents = abstractionForAgents;
		
		agents = new ArrayList<SGAgent>();
		agentsByType = new HashMap<SGAgentType, List<SGAgent>>();
		this.agentDefinitions = new HashMap<String, SGAgentType>();
		
		agentCumulativeReward = new HashedAggregator<String>();
		
		worldObservers = new ArrayList<WorldObserver>();

		this.generateNewCurrentState();
		
		debugId = 284673923;
	}

	public SGDomain getDomain() {
		return domain;
	}

	public void setDomain(SGDomain domain) {
		this.domain = domain;
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
		return agentCumulativeReward.v(aname);
	}
	
	
	/**
	 * Registers an agent to be a participant in this world.
	 * @param a the agent to be registered in this world
	 * @param at the agent type the agent will be playing as
	 * @return the unique name that will identify this agent in this world.
	 */
	public String registerAgent(SGAgent a, SGAgentType at){
		//don't register the same agent multiple times
		if(this.agentInstanceExists(a)){
			return a.getAgentName();
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
	 * Causes the world to set the current state to a state generated by the provided {@link SGStateGenerator} object
	 * if a game is not currently running. If a game is currently running, then the state will not be changed.
	 */
	public void generateNewCurrentState(){
		if(!this.gameIsRunning()) {
			currentState = initialStateGenerator.generateState(agents);
		}
	}


	/**
	 * Returns whether the current state in the world is a terminal state or not.
	 * @return true if the current world state is terminal; false otherwise.
	 */
	public boolean worldStateIsTerminal(){
		return this.tf.isTerminal(this.currentState);
	}

	/**
	 * Sets the world state to the provided state if the a game is not currently running. If a game is running,
	 * then no change will occur.
	 * @param s the state to which the world will be set.
	 */
	public void setCurrentState(State s){
		if(!this.gameIsRunning()){
			currentState = s;
		}
	}
	
	/**
	 * Returns the last joint action taken in this world; null if none have been taken yet.
	 * @return the last joint action taken in this world; null if none have been taken yet
	 */
	public JointAction getLastJointAction(){
		return this.lastJointAction;
	}


	/**
	 * Returns the last rewards received.
	 * @return the last rewards reward delivered to each agent.
	 */
	public Map<String, Double> getLastRewards() {
		return lastRewards;
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
	 * Manually attempts to execute a joint action in the current world state, if a game is currently not running.
	 * If a game is running, then no action will be taken. Additionally, if the world is currently in a terminal
	 * state, then no action will be taken either.
	 * @param ja the {@link JointAction} to execute.
	 */
	public void executeJointAction(JointAction ja){

		if(!this.gameIsRunning()){
			if(tf.isTerminal(currentState)){
				return ; //cannot continue this game
			}

			State oldState = this.currentState;
			this.currentState = this.worldModel.sample(this.currentState, ja);
			Map<String,Double> rewards = this.jointRewardFunction.reward(oldState, ja, this.currentState);
			this.lastRewards = rewards;
			this.lastJointAction = ja;

			for(WorldObserver ob : this.worldObservers){
				ob.observe(oldState, ja, rewards, this.currentState);
			}

		}

	}

	/**
	 * Runs a game until a terminal state is hit.
	 * @return a {@link burlap.behavior.stochasticgames.GameAnalysis} of the game.
	 */
	public GameAnalysis runGame(){
		return this.runGame(-1);
	}
	
	/**
	 * Runs a game until a terminal state is hit for maxStages have occurred
	 * @param maxStages the maximum number of stages to play in the game before its forced to end. If set to -1, then run until a terminal state is hit.
	 * @return a {@link burlap.behavior.stochasticgames.GameAnalysis} of the game.
	 */
	public GameAnalysis runGame(int maxStages){
		
		return this.runGame(maxStages, initialStateGenerator.generateState(agents));
		
	}


	/**
	 * Runs a game starting in the input state until a terminal state is hit.
	 * @param maxStages the maximum number of stages to play in the game before its forced to end. If set to -1, then run until a terminal state is hit.
	 * @param s the input {@link State} from which the game will start
	 * @return a {@link burlap.behavior.stochasticgames.GameAnalysis} of the game.
	 */
	public GameAnalysis runGame(int maxStages, State s){

		for(SGAgent a : agents){
			a.gameStarting();
		}

		currentState = s;
		this.currentGameRecord = new GameAnalysis(currentState);
		this.isRecordingGame = true;

		int t = 0;



		for(WorldObserver wob : this.worldObservers){
			wob.gameStarting(this.currentState);
		}

		while(!tf.isTerminal(currentState) && (t < maxStages || maxStages == -1)){
			this.runStage();
			t++;
		}

		for(SGAgent a : agents){
			a.gameTerminated();
		}

		for(WorldObserver wob : this.worldObservers){
			wob.gameEnding(this.currentState);
		}

		DPrint.cl(debugId, currentState.toString());

		this.isRecordingGame = false;

		return this.currentGameRecord;
	}
	
	/**
	 * Rollsout a joint policy until a terminate state is reached for a maximum number of stages.
	 * @param jp the joint policy to rollout
	 * @param maxStages the maximum number of stages
	 * @return a {@link GameAnalysis} that has recorded the result.
	 */
	public GameAnalysis rolloutJointPolicy(JointPolicy jp, int maxStages){
		currentState = initialStateGenerator.generateState(agents);
		this.currentGameRecord = new GameAnalysis(currentState);
		this.isRecordingGame = true;
		int t = 0;
		
		while(!tf.isTerminal(currentState) && t < maxStages){
			this.rolloutOneStageOfJointPolicy(jp);
			t++;
		}
		
		this.isRecordingGame = false;
		
		return this.currentGameRecord;
	}
	
	
	
	/**
	 * Rollsout a joint policy from a given state until a terminate state is reached for a maximum number of stages.
	 * @param jp the joint policy to rollout
	 * @param s the state from which the joint policy should be rolled out
	 * @param maxStages the maximum number of stages
	 * @return a {@link GameAnalysis} that has recorded the result.
	 */
	public GameAnalysis rolloutJointPolicyFromState(JointPolicy jp, State s, int maxStages){
		currentState = s;
		this.currentGameRecord = new GameAnalysis(currentState);
		this.isRecordingGame = true;
		int t = 0;
		
		while(!tf.isTerminal(currentState) && t < maxStages){
			this.rolloutOneStageOfJointPolicy(jp);
			t++;
		}
		
		this.isRecordingGame = false;
		
		return this.currentGameRecord;
	}
	
	/**
	 * Runs a single stage of this game.
	 */
	public void runStage(){


		if(tf.isTerminal(currentState)){
			return ; //cannot continue this game
		}
		
		JointAction ja = new JointAction();
		State abstractedCurrent = abstractionForAgents.mapState(currentState);
		for(SGAgent a : agents){
			ja.addAction(a.getAction(abstractedCurrent));
		}
		this.lastJointAction = ja;
		
		
		DPrint.cl(debugId, ja.toString());
		
		
		//now that we have the joint action, perform it
		State sp = worldModel.sample(currentState, ja);
		State abstractedPrime = this.abstractionForAgents.mapState(sp);
		Map<String, Double> jointReward = jointRewardFunction.reward(currentState, ja, sp);
		
		DPrint.cl(debugId, jointReward.toString());
		
		//index reward
		for(Entry<String, Double> ar : jointReward.entrySet()){
			double r = ar.getValue();
			agentCumulativeReward.add(ar.getKey(), r);
		}
		
		//tell all the agents about it
		for(SGAgent a : agents){
			a.observeOutcome(abstractedCurrent, ja, jointReward, abstractedPrime, tf.isTerminal(sp));
		}
		
		//tell observers
		for(WorldObserver o : this.worldObservers){
			o.observe(currentState, ja, jointReward, sp);
		}
		
		//update the state
		currentState = sp;
		this.lastRewards = jointReward;
		
		//record events
		if(this.isRecordingGame){
			this.currentGameRecord.recordTransitionTo(this.lastJointAction, this.currentState, jointReward);
		}
		
	}


	/**
	 * Runs a single stage following a joint policy for the current world state
	 * @param jp the joint policy to follow
	 */
	protected void rolloutOneStageOfJointPolicy(JointPolicy jp){
		
		if(tf.isTerminal(currentState)){
			return ; //cannot continue this game
		}
		
		this.lastJointAction = (JointAction)jp.getAction(this.currentState);
		
		DPrint.cl(debugId, this.lastJointAction.toString());
		
		
		//now that we have the joint action, perform it
		State sp = worldModel.sample(currentState, this.lastJointAction);
		Map<String, Double> jointReward = jointRewardFunction.reward(currentState, this.lastJointAction, sp);
		
		DPrint.cl(debugId, jointReward.toString());
		
		//index reward
		for(Entry<String, Double> ar : jointReward.entrySet()){
			double r = ar.getValue();
			agentCumulativeReward.add(ar.getKey(), r);
		}
		
		
		//tell observers
		for(WorldObserver o : this.worldObservers){
			o.observe(currentState, this.lastJointAction, jointReward, sp);
		}
		
		//update the state
		currentState = sp;
		this.lastRewards = jointReward;
		
		//record events
		if(this.isRecordingGame){
			this.currentGameRecord.recordTransitionTo(this.lastJointAction, this.currentState, jointReward);
		}
		
	}
	
	/**
	 * Returns the {@link JointActionModel} used in this world.
	 * @return the {@link JointActionModel} used in this world.
	 */
	public JointActionModel getActionModel(){
		return worldModel;
	}
	
	
	/**
	 * Returns the {@link JointRewardFunction} function used in this world.
	 * @return the {@link JointRewardFunction} function used in this world.
	 */
	public JointRewardFunction getRewardFunction(){
		return jointRewardFunction;
	}
	
	/**
	 * Returns the {@link burlap.mdp.core.TerminalFunction} used in this world.
	 * @return the {@link burlap.mdp.core.TerminalFunction} used in this world.
	 */
	public TerminalFunction getTF(){
		return tf;
	}
	
	
	/**
	 * Returns the list of agents participating in this world.
	 * @return the list of agents participating in this world.
	 */
	public List <SGAgent> getRegisteredAgents(){
		return new ArrayList<SGAgent>(agents);
	}
	
	
	/**
	 * Returns the agent definitions for the agents registered in this world.
	 * @return the agent definitions for the agents registered in this world.
	 */
	public Map<String, SGAgentType> getAgentDefinitions(){
		return this.agentDefinitions;
	}
	
	
	/**
	 * Returns the player index for the agent with the given name.
	 * @param aname the name of the agent
	 * @return the player index of the agent with the given name.
	 */
	public int getPlayerNumberForAgent(String aname){
		for(int i = 0; i < agents.size(); i++){
			SGAgent a = agents.get(i);
			if(a.getAgentName().equals(aname)){
				return i;
			}
		}
		
		return -1;
	}


	/**
	 * Returns whether a game in this world is currently running.
	 * @return true if a game is running, false otherwise.
	 */
	public boolean gameIsRunning(){
		return this.isRecordingGame;
	}
	
	
	/**
	 * Returns a unique agent name for the given agent object and agent type for that agent.
	 * @param a the agent for which a unique name is to be returned
	 * @param type the agent type of the agent
	 * @return a unique name for the agent
	 */
	protected String getNewWorldNameForAgentAndIndex(SGAgent a, SGAgentType type){
	
		
		List <SGAgent> aots = agentsByType.get(type);
		if(aots == null){
			aots = new ArrayList<SGAgent>();
			agentsByType.put(type, aots);
		}
		
		String name = type.typeName + aots.size();
		agents.add(a);
		aots.add(a);
		
		
		this.agentDefinitions.put(name, type);
		
		return name;
	}
	
	
	/**
	 * Returns whether the reference for the given agent already exists in the registered agents
	 * @param a the agent reference to check for
	 * @return true if that agent reference is already registered; false otherwise
	 */
	protected boolean agentInstanceExists(SGAgent a){
		for(SGAgent A : agents){
			if(A == a){
				return true;
			}
		}
		
		return false;
	}

}
