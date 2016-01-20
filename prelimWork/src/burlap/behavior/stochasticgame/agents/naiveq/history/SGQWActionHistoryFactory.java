package burlap.behavior.stochasticgame.agents.naiveq.history;

import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.stochasticgames.Agent;
import burlap.oomdp.stochasticgames.AgentFactory;
import burlap.oomdp.stochasticgames.SGDomain;


/**
 * An agent factory for Q-learning with history agents.
 * @author James MacGlashan
 *
 */
public class SGQWActionHistoryFactory implements AgentFactory {

	/**
	 * The stochastic games domain in which the agent will act
	 */
	protected SGDomain													domain;
	
	/**
	 * The discount rate the Q-learning algorithm will use
	 */
	protected double													discount;
	
	/**
	 * The learning rate the Q-learning algorithm will use
	 */
	protected double													learningRate;
	
	
	/**
	 * The state hashing factory the Q-learning algorithm will use
	 */
	protected StateHashFactory											stateHash; 
	
	/**
	 * How much history the agent should remember
	 */
	protected int														historySize;
	
	/**
	 * The maximum number of players that can be in the game
	 */
	protected int														maxPlayers;
	
	/**
	 * An action mapping to map from actions to int values
	 */
	protected ActionIdMap												actionMap;
	
	
	
	/**
	 * Initializes the factory
	 * @param d the stochastic games domain in which the agent will act
	 * @param discount The discount rate the Q-learning algorithm will use
	 * @param learningRate The learning rate the Q-learning algorithm will use
	 * @param stateHash The state hashing factory the Q-learning algorithm will use
	 * @param historySize How much history the agent should remember
	 * @param maxPlayers The maximum number of players that can be in the game
	 * @param actionMap An action mapping to map from actions to int values
	 */
	public SGQWActionHistoryFactory(SGDomain d, double discount, double learningRate, StateHashFactory stateHash, int historySize, int maxPlayers, ActionIdMap actionMap) {
		this.domain = d;
		this.learningRate = learningRate;
		this.stateHash = stateHash;
		this.historySize = historySize;
		this.maxPlayers = maxPlayers;
		this.actionMap = actionMap;
	}

	@Override
	public Agent generateAgent() {
		return new SGQWActionHistory(domain, discount, learningRate, stateHash, historySize, maxPlayers, actionMap);
	}

}
