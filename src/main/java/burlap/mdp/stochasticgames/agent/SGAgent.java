package burlap.mdp.stochasticgames.agent;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.world.World;


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
public interface SGAgent {

	
	/**
	 * Returns this agent's name
	 * @return this agent's name
	 */
	String agentName();
	
	
	/**
	 * Returns this agent's type
	 * @return this agent's type
	 */
	SGAgentType agentType();

	/**
	 * This method is called by the world when a new game is starting.
	 * @param w the world in which the game is starting
	 * @param agentNum the agent number of the agent in the world
	 */
	void gameStarting(World w, int agentNum);
	
	/**
	 * This method is called by the world when it needs the agent to choose an action
	 * @param s the current state of the world
	 * @return the action this agent wishes to take
	 */
	Action action(State s);
	
	/**
	 * This method is called by the world when every agent in the world has taken their action. It conveys the result of
	 * the joint action.
	 * @param s the state in which the last action of each agent was taken
	 * @param jointAction the joint action of all agents in the world
	 * @param jointReward the joint reward of all agents in the world
	 * @param sprime the next state to which the agent transitioned
	 * @param isTerminal whether the new state is a terminal state
	 */
	void observeOutcome(State s, JointAction jointAction, double[] jointReward, State sprime, boolean isTerminal);
	
	
	/**
	 * This method is called by the world when a game has ended.
	 */
	void gameTerminated();

}
