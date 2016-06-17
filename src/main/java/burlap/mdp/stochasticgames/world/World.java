package burlap.mdp.stochasticgames.world;

import burlap.behavior.stochasticgames.GameEpisode;
import burlap.behavior.stochasticgames.JointPolicy;
import burlap.datastructures.HashedAggregator;
import burlap.debugtools.DPrint;
import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.auxiliary.common.IdentityStateMapping;
import burlap.mdp.core.TerminalFunction;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.SGDomain;
import burlap.mdp.stochasticgames.SGStateGenerator;
import burlap.mdp.stochasticgames.agent.SGAgent;
import burlap.mdp.stochasticgames.agent.SGAgentType;
import burlap.mdp.stochasticgames.common.ConstantSGStateGenerator;
import burlap.mdp.stochasticgames.model.JointModel;
import burlap.mdp.stochasticgames.model.JointRewardFunction;

import java.util.ArrayList;
import java.util.List;


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
	protected List <SGAgent>					agents;
	protected HashedAggregator<String>			agentCumulativeReward;
	
	protected JointModel worldModel;
	protected JointRewardFunction jointRewardFunction;
	protected TerminalFunction					tf;
	protected SGStateGenerator initialStateGenerator;
	
	protected StateMapping					abstractionForAgents;
	
	
	protected JointAction lastJointAction;
	
	
	protected List<WorldObserver>				worldObservers;
	
	
	protected GameEpisode currentGameEpisodeRecord;
	protected boolean							isRecordingGame = false;
	
	protected int								debugId;

	protected double[]							lastRewards;




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
	
	protected void init(SGDomain domain, JointModel jam, JointRewardFunction jr, TerminalFunction tf, SGStateGenerator sg, StateMapping abstractionForAgents){
		this.domain = domain;
		this.worldModel = jam;
		this.jointRewardFunction = jr;
		this.tf = tf;
		this.initialStateGenerator = sg;
		this.abstractionForAgents = abstractionForAgents;
		
		agents = new ArrayList<SGAgent>();
		
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
	 * Causes the provided agent to join the world
	 * @param a the agent to join
	 */
	public void join(SGAgent a){

		if(this.agentWithName(a.agentName()) != null){
			throw new RuntimeException("Agent with provided name has already joined.");
		}
		agents.add(a);

	}

	/**
	 * Returns the agent with the given name, or null if there is no agent with that name.
	 * @param name the name of the agent
	 * @return the {@link SGAgent} with the name in this world, or null if there is none.
	 */
	public SGAgent agentWithName(String name){
		for(SGAgent agent : agents){
			if(agent.agentName().equals(name)){
				return agent;
			}
		}
		return null;
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
	public double[] getLastRewards() {
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
			double[] rewards = this.jointRewardFunction.reward(oldState, ja, this.currentState);
			this.lastRewards = rewards;
			this.lastJointAction = ja;

			for(WorldObserver ob : this.worldObservers){
				ob.observe(oldState, ja, rewards, this.currentState);
			}

		}

	}

	/**
	 * Runs a game until a terminal state is hit.
	 * @return a {@link GameEpisode} of the game.
	 */
	public GameEpisode runGame(){
		return this.runGame(-1);
	}
	
	/**
	 * Runs a game until a terminal state is hit for maxStages have occurred
	 * @param maxStages the maximum number of stages to play in the game before its forced to end. If set to -1, then run until a terminal state is hit.
	 * @return a {@link GameEpisode} of the game.
	 */
	public GameEpisode runGame(int maxStages){
		
		return this.runGame(maxStages, initialStateGenerator.generateState(agents));
		
	}


	/**
	 * Runs a game starting in the input state until a terminal state is hit.
	 * @param maxStages the maximum number of stages to play in the game before its forced to end. If set to -1, then run until a terminal state is hit.
	 * @param s the input {@link State} from which the game will start
	 * @return a {@link GameEpisode} of the game.
	 */
	public GameEpisode runGame(int maxStages, State s){

		int aid = 0;
		for(SGAgent a : agents){
			a.gameStarting(this, aid);
			aid++;
		}

		currentState = s;
		this.currentGameEpisodeRecord = new GameEpisode(currentState);
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

		return this.currentGameEpisodeRecord;
	}
	
	/**
	 * Rollsout a joint policy until a terminate state is reached for a maximum number of stages.
	 * @param jp the joint policy to rollout
	 * @param maxStages the maximum number of stages
	 * @return a {@link GameEpisode} that has recorded the result.
	 */
	public GameEpisode rolloutJointPolicy(JointPolicy jp, int maxStages){
		currentState = initialStateGenerator.generateState(agents);
		this.currentGameEpisodeRecord = new GameEpisode(currentState);
		this.isRecordingGame = true;
		int t = 0;
		
		while(!tf.isTerminal(currentState) && t < maxStages){
			this.rolloutOneStageOfJointPolicy(jp);
			t++;
		}
		
		this.isRecordingGame = false;
		
		return this.currentGameEpisodeRecord;
	}
	
	
	
	/**
	 * Rollsout a joint policy from a given state until a terminate state is reached for a maximum number of stages.
	 * @param jp the joint policy to rollout
	 * @param s the state from which the joint policy should be rolled out
	 * @param maxStages the maximum number of stages
	 * @return a {@link GameEpisode} that has recorded the result.
	 */
	public GameEpisode rolloutJointPolicyFromState(JointPolicy jp, State s, int maxStages){
		currentState = s;
		this.currentGameEpisodeRecord = new GameEpisode(currentState);
		this.isRecordingGame = true;
		int t = 0;
		
		while(!tf.isTerminal(currentState) && t < maxStages){
			this.rolloutOneStageOfJointPolicy(jp);
			t++;
		}
		
		this.isRecordingGame = false;
		
		return this.currentGameEpisodeRecord;
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
			ja.addAction(a.action(abstractedCurrent));
		}
		this.lastJointAction = ja;
		
		
		DPrint.cl(debugId, ja.toString());
		
		
		//now that we have the joint action, perform it
		State sp = worldModel.sample(currentState, ja);
		State abstractedPrime = this.abstractionForAgents.mapState(sp);
		double[] jointReward = jointRewardFunction.reward(currentState, ja, sp);
		
		DPrint.cl(debugId, jointReward.toString());

		//index reward
		for(int i = 0; i < jointReward.length; i++){
			String agentName = this.agents.get(i).agentName();
			agentCumulativeReward.add(agentName, jointReward[i]);
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
			this.currentGameEpisodeRecord.transition(this.lastJointAction, this.currentState, jointReward);
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
		
		this.lastJointAction = (JointAction)jp.action(this.currentState);
		
		DPrint.cl(debugId, this.lastJointAction.toString());
		
		
		//now that we have the joint action, perform it
		State sp = worldModel.sample(currentState, this.lastJointAction);
		double[] jointReward = jointRewardFunction.reward(currentState, this.lastJointAction, sp);
		
		DPrint.cl(debugId, jointReward.toString());

		//index reward
		for(int i = 0; i < jointReward.length; i++){
			String agentName = this.agents.get(i).agentName();
			agentCumulativeReward.add(agentName, jointReward[i]);
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
			this.currentGameEpisodeRecord.transition(this.lastJointAction, this.currentState, jointReward);
		}
		
	}
	
	/**
	 * Returns the {@link JointModel} used in this world.
	 * @return the {@link JointModel} used in this world.
	 */
	public JointModel getActionModel(){
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
	public List<SGAgentType> getAgentDefinitions(){

//		Map<String, SGAgentType> types = new HashMap<String, SGAgentType>(agents.size());
//		for(SGAgent agent : this.agents){
//			types.put(agent.agentName(), agent.agentType());
//		}
//		return types;

		List<SGAgentType> defs = new ArrayList<SGAgentType>(this.agents.size());
		for(SGAgent a : this.agents){
			defs.add(a.agentType());
		}
		return defs;
	}
	
	
	/**
	 * Returns the player index for the agent with the given name.
	 * @param aname the name of the agent
	 * @return the player index of the agent with the given name.
	 */
	public int getPlayerNumberForAgent(String aname){
		for(int i = 0; i < agents.size(); i++){
			SGAgent a = agents.get(i);
			if(a.agentName().equals(aname)){
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

	


}
