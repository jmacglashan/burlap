package burlap.behavior.stochasticgames.agents.naiveq.history;

import burlap.behavior.valuefunction.ValueFunctionInitialization;
import burlap.behavior.policy.EpsilonGreedy;
import burlap.oomdp.statehashing.HashableStateFactory;
import burlap.oomdp.stochasticgames.SGAgent;
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
	protected HashableStateFactory stateHash;
	
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
	protected ActionIdMap												actionMap = null;
	
	/**
	 * A default Q-value initializer
	 */
	protected ValueFunctionInitialization								qinit = null;
	
	/**
	 * The epislon value for epislon greedy policy. If negative, then the policy of the created agent
	 * will not be different than its default.
	 */
	protected double													epsilon = -1.;
	
	
	
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
	public SGQWActionHistoryFactory(SGDomain d, double discount, double learningRate, HashableStateFactory stateHash, int historySize, int maxPlayers, ActionIdMap actionMap) {
		this.domain = d;
		this.learningRate = learningRate;
		this.stateHash = stateHash;
		this.historySize = historySize;
		this.maxPlayers = maxPlayers;
		this.actionMap = actionMap;
	}
	
	/**
	 * Initializes the factory
	 * @param d the stochastic games domain in which the agent will act
	 * @param discount The discount rate the Q-learning algorithm will use
	 * @param learningRate The learning rate the Q-learning algorithm will use
	 * @param stateHash The state hashing factory the Q-learning algorithm will use
	 * @param historySize How much history the agent should remember
	 */
	public SGQWActionHistoryFactory(SGDomain d, double discount, double learningRate, HashableStateFactory stateHash, int historySize) {
		this.domain = d;
		this.learningRate = learningRate;
		this.stateHash = stateHash;
		this.historySize = historySize;
	}
	
	/**
	 * Sets the Q-value initialization function that will be used by the agent.
	 * @param qinit the Q-value initialization function.
	 */
	public void setQValueInitializer(ValueFunctionInitialization qinit){
		this.qinit = qinit;
	}
	
	/**
	 * Sets the epislon parmaeter (for epsilon greedy policy). If set to a negative, then the default policy of the create agent will be used.
	 * @param epsilon the epsilon value to use
	 */
	public void setEpsilon(double epsilon){
		this.epsilon = epsilon;
	}

	@Override
	public SGAgent generateAgent() {
		SGQWActionHistory agent;
		if(this.actionMap != null){
			agent = new SGQWActionHistory(domain, discount, learningRate, stateHash, historySize, maxPlayers, actionMap);
		}
		else{
			agent = new SGQWActionHistory(domain, discount, learningRate, stateHash, historySize);
		}
		if(this.qinit != null){
			agent.setQValueInitializer(qinit);
		}
		if(this.epsilon >= 0.){
			EpsilonGreedy egreedy = new EpsilonGreedy(agent, this.epsilon);
			agent.setStrategy(egreedy);
		}
		
		return agent;
		
	}

}
