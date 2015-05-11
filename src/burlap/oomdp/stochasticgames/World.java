package burlap.oomdp.stochasticgames;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import burlap.behavior.stochasticgame.GameAnalysis;
import burlap.behavior.stochasticgame.JointPolicy;
import burlap.datastructures.HashedAggregator;
import burlap.debugtools.DPrint;
import burlap.oomdp.auxiliary.StateAbstraction;
import burlap.oomdp.auxiliary.common.NullAbstraction;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.stochasticgames.Callables.GameStartingCallable;
import burlap.oomdp.stochasticgames.Callables.GetActionCallable;
import burlap.oomdp.stochasticgames.Callables.ObserveOutcomeCallable;
import burlap.parallel.Parallel;
import burlap.parallel.Parallel.ForEachCallable;


/**
 * This class provides a means to have agents play against each other and synchronize all of their actions and observations.
 * Any number of agents can join a World instance and they will be told when a game is starting, when a game ends, when
 * they need to provide an action, and what happened to all agents after every agent made their action selection. The world
 * may also make use of an optional {@link burlap.oomdp.auxiliary.StateAbstraction} object so that agents are provided an 
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
	protected HashedAggregator<String>			agentCumulativeReward;
	protected Map<String, AgentType>			agentDefinitions;
	
	protected JointActionModel 					worldModel;
	protected JointReward						jointRewardModel;
	protected TerminalFunction					tf;
	protected SGStateGenerator					initialStateGenerator;
	
	protected StateAbstraction					abstractionForAgents;
	
	
	protected JointAction						lastJointAction;
	
	
	protected List<WorldObserver>				worldObservers;
	
	
	protected GameAnalysis						currentGameRecord;
	protected boolean							isRecordingGame = false;
	
	protected int								debugId;
	protected double							threadTimeout;
	protected String							worldDescription;
	private boolean								isOk;
	private ReentrantLock					isOkLock;
	
	
	
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
		this.agentDefinitions = new HashMap<String, AgentType>();
		
		agentCumulativeReward = new HashedAggregator<String>();
		
		worldObservers = new ArrayList<WorldObserver>();
		
		debugId = 284673923;
		this.threadTimeout = Parallel.NO_TIME_LIMIT;
		this.isOkLock = new ReentrantLock();
		this.isOk = true;
	}
	
	public World copy() {
		World copy = new World(this.domain, this.worldModel, this.jointRewardModel, this.tf, this.initialStateGenerator, this.abstractionForAgents);
		copy.setDescription(this.worldDescription);
		return copy;
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
	 * Sets the timeout for threads that call an agent's getAction methods. -1.0 is currently no time limit
	 * @param timeout
	 */
	public void setThreadTimeout(double timeout) {
		if (timeout < 0) {
			this.threadTimeout = Parallel.NO_TIME_LIMIT;
		}
		this.threadTimeout = timeout;
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
	protected String registerAgent(Agent a, AgentType at){
		//don't register the same agent multiple times
		if(this.agentInstanceExists(a)){
			return a.worldAgentName;
		}
		
		String agentName = this.getNewWorldNameForAgentAndIndex(a, at);
		
		return agentName;
		
	}
	
	public SGDomain getDomain() {
		return this.domain;
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
	 * Generates an example starting state;
	 * @return
	 */
	public State startingState() {
		State startingState = this.initialStateGenerator.generateState(this.agents);
		StringBuilder builder  = new StringBuilder();
		for (ObjectInstance object : startingState.getAllObjects()) {
			for (String attribute : object.unsetAttributes()) {
				builder.append(object.getName()).append(" - ").append(attribute).append("\n");
			}
		}
		if (builder.length() > 0) {
			throw new RuntimeException("The state generator for world " + this.toString() +  " passes incomplete states. The following attributes are unset: \n" + builder.toString());
		}
		
		return this.abstractionForAgents.abstraction(startingState);
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
	
	private void setOk(boolean value) {
		this.isOkLock.lock();
		this.isOk = value;
		this.isOkLock.unlock();
	}
	
	public boolean isOk() {
		this.isOkLock.lock();
		boolean value = this.isOk;
		this.isOkLock.unlock();
		return value;
	}
	
	
	/**
	 * Runs a game until a terminal state is hit.
	 */
	public GameAnalysis runGame(){
		
		return this.runGame(-1);
		/*
		for(Agent a : agents){
			a.gameStarting();
		}
		
		currentState = initialStateGenerator.generateState(agents);
		this.currentGameRecord = new GameAnalysis(currentState);
		this.isRecordingGame = true;
		
		while(!tf.isTerminal(currentState)){
			this.runStage();
		}
		
		for(Agent a : agents){
			a.gameTerminated();
		}
		
		DPrint.cl(debugId, currentState.getCompleteStateDescription());
		
		this.isRecordingGame = false;
		
		return this.currentGameRecord;*/
	}
	
	/**
	 * Runs a game until a terminal state is hit for maxStages have occurred
	 * @param maxStages the maximum number of stages to play in the game before its forced to end.
	 */
	public GameAnalysis runGame(int maxStages){
		this.setOk(true);
		currentState = initialStateGenerator.generateState(agents);
		
		ForEachCallable<Agent, Boolean> gameStarting = new GameStartingCallable(currentState, this.abstractionForAgents);
		Parallel.ForEach(agents, gameStarting);
		/*
		for(Agent a : agents){
			a.gameStarting();
		}*/
		
		this.currentGameRecord = new GameAnalysis(currentState);
		this.isRecordingGame = true;
		int t = 0;
		
		while(!tf.isTerminal(currentState) && (maxStages < 0 || t < maxStages) && this.isOk){
			this.runStage();
			t++;
		}
		
		// call to agents needs to be threaded, and timed out
		for(Agent a : agents){
			a.gameTerminated();
		}
		
		// clean up threading
		
		DPrint.cl(debugId, currentState.getCompleteStateDescription());
		
		this.isRecordingGame = false;
		
		return this.currentGameRecord;
		
	}
	
	public void stopGame() {
		this.setOk(false);
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
		if (this.currentState == null) {
			throw new RuntimeException("The current state has not been set.");
		}
		if(tf.isTerminal(currentState)){
			return ; //cannot continue this game
		}
		
		JointAction ja = new JointAction();
		State abstractedCurrent = abstractionForAgents.abstraction(currentState);
		
		// Call to agents needs to be threaded, and timed out
		ForEachCallable<List<Agent>, List<GroundedSingleAction>> getActionCallable = new GetActionCallable(abstractedCurrent);
		
		// Agents that can threaded independently of any others should have their own list
		List<List<Agent>> agentsByThread = new ArrayList<List<Agent>>();
		
		// All agents that share a common library, or for some reason can't be run on a different thread as another must go in this list
		List<Agent> singleThreadedAgents = new ArrayList<Agent>();
		
		// Check all agents for their parallizability
		for (Agent agent : agents) {
			if (agent.canBeThreaded()) {
				agentsByThread.add(Arrays.asList(agent));
			} else {
				singleThreadedAgents.add(agent);
			}
		}
		// Randomize order so if not all agents can run in the alloted time, it's at least different each run
		Collections.shuffle(singleThreadedAgents);
		agentsByThread.add(singleThreadedAgents);
		
		// Run agents on separate threads if possible
		List<List<GroundedSingleAction>> allActions = Parallel.ForEach(agentsByThread, getActionCallable, this.threadTimeout);
		
		
		for (List<GroundedSingleAction> actions : allActions) {
			if (actions == null) {
				continue;
			}
			for (GroundedSingleAction action : actions) {
				if (action != null) {
					ja.addAction(action);
				}
			}
		}
			
		/*for(Agent a : agents){
			ja.addAction(a.getAction(abstractedCurrent));
		}*/
		this.lastJointAction = ja;
		
		
		DPrint.cl(debugId, ja.toString());
		
		
		//now that we have the joint action, perform it
		State sp = worldModel.performJointAction(currentState, ja);
		//State abstractedPrime = this.abstractionForAgents.abstraction(sp);
		Map<String, Double> jointReward = jointRewardModel.reward(currentState, ja, sp);
		
		DPrint.cl(debugId, jointReward.toString());
		
		//index reward
		for(String aname : jointReward.keySet()){
			double r = jointReward.get(aname);
			agentCumulativeReward.add(aname, r);
		}
		
		// This needs to be threaded, and timed out
		//tell all the agents about it
		ForEachCallable<Agent, Boolean> callable = new ObserveOutcomeCallable(currentState, ja, jointReward, sp, this.abstractionForAgents, tf.isTerminal(sp));
		Parallel.ForEach(agents, callable);
		/*for(Agent a : agents){
			a.observeOutcome(abstractedCurrent, ja, jointReward, abstractedPrime, tf.isTerminal(sp));
		}*/
		
		// Maybe need to be threaded, and timed out.
		//tell observers
		for(WorldObserver o : this.worldObservers){
			o.observe(currentState, ja, jointReward, sp);
		}
		
		//update the state
		currentState = sp;
		
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
		State sp = worldModel.performJointAction(currentState, this.lastJointAction);
		Map<String, Double> jointReward = jointRewardModel.reward(currentState, this.lastJointAction, sp);
		
		DPrint.cl(debugId, jointReward.toString());
		
		//index reward
		for(String aname : jointReward.keySet()){
			double r = jointReward.get(aname);
			agentCumulativeReward.add(aname, r);
		}
		
		
		//tell observers
		for(WorldObserver o : this.worldObservers){
			o.observe(currentState, this.lastJointAction, jointReward, sp);
		}
		
		//update the state
		currentState = sp;
		
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
	 * Returns the agent definitions for the agents registered in this world.
	 * @return the agent definitions for the agents registered in this world.
	 */
	public Map<String, AgentType> getAgentDefinitions(){
		return this.agentDefinitions;
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
	
	
	/**
	 * Returns a unique agent name for the given agent object and agent type for that agent.
	 * @param a the agent for which a unique name is to be returned
	 * @param type the agent type of the agent
	 * @return a unique name for the agent
	 */
	protected String getNewWorldNameForAgentAndIndex(Agent a, AgentType type){
	
		
		List <Agent> aots = agentsByType.get(type);
		if(aots == null){
			aots = new ArrayList<Agent>();
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
	protected boolean agentInstanceExists(Agent a){
		for(Agent A : agents){
			if(A == a){
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		return (this.worldDescription == null) ? "" : this.worldDescription;
	}

	public void setDescription(String description) {
		this.worldDescription = description;
	}
}
