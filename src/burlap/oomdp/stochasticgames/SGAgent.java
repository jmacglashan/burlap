package burlap.oomdp.stochasticgames;

import java.util.Map;

import burlap.oomdp.core.states.State;


/**
 * This abstract class defines the the shell code and interface for creating agents
 * that can make decisions in mutli-agent stochastic game worlds. Agents have
 * names to identify themselves and can joint multi-agent worlds as a specific 
 * type of agent that is specified by their {@link SGAgentType}. Agents are informed
 * by the world when a game is starting and ending and they are also queried
 * by the world for the action they will take. After all agents in the world
 * have chosen their action, the world changes and informs each agent of the outcome
 * for all agents in the world.
 * @author James MacGlashan
 *
 */
public abstract class SGAgent {

	protected SGDomain				domain;
	protected JointReward			internalRewardFunction;
	
	
	//data members for interaction with the world
	protected SGAgentType agentType;
	protected String				worldAgentName;
	protected World					world;
	
	
	protected void init(SGDomain d){
		this.domain = d;
		internalRewardFunction = null;
	}
	
	
	/**
	 * Internal reward functions are optional, but can be useful for purposes like reward shaping.
	 * @param jr the internal reward function the agent should use for reasoning and learning
	 */
	public void setInternalRewardFunction(JointReward jr){
		this.internalRewardFunction = jr;
	}
	
	/**
	 * Returns the internal reward function used by the agent.
	 * @return the internal reward function used by the agent; null if the agent is not using an internal reward function.
	 */
	public JointReward getInternalRewardFunction() {
		return this.internalRewardFunction;
	}
	
	
	/**
	 * Causes this agent instance to join a world.
	 * @param w the world for the agent to join
	 * @param as the agent type the agent will be joining as
	 */
	public void joinWorld(World w, SGAgentType as){
		agentType = as;
		world = w;
		worldAgentName = world.registerAgent(this, as);
	}
	
	
	/**
	 * Returns this agent's name
	 * @return this agent's name
	 */
	public String getAgentName(){
		return worldAgentName;
	}
	
	
	/**
	 * Returns this agent's type
	 * @return this agent's type
	 */
	public SGAgentType getAgentType(){
		return agentType;
	}
	
	/**
	 * This method is called by the world when a new game is starting.
	 */
	public abstract void gameStarting();
	
	/**
	 * This method is called by the world when it needs the agent to choose an action
	 * @param s the current state of the world
	 * @return the action this agent wishes to take
	 */
	public abstract GroundedSGAgentAction getAction(State s);
	
	/**
	 * This method is called by the world when every agent in the world has taken their action. It conveys the result of
	 * the joint action.
	 * @param s the state in which the last action of each agent was taken
	 * @param jointAction the joint action of all agents in the world
	 * @param jointReward the joint reward of all agents in the world
	 * @param sprime the next state to which the agent transitioned
	 * @param isTerminal whether the new state is a terminal state
	 */
	public abstract void observeOutcome(State s, JointAction jointAction, Map<String, Double> jointReward, State sprime, boolean isTerminal);
	
	
	/**
	 * This method is called by the world when a game has ended.
	 */
	public abstract void gameTerminated();

}
