package burlap.behavior.stochasticgame.agents.naiveq;

import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.auxiliary.StateAbstraction;
import burlap.oomdp.stochasticgames.Agent;
import burlap.oomdp.stochasticgames.AgentFactory;
import burlap.oomdp.stochasticgames.SGDomain;


/**
 * An agent factory that produces {@link SGNaiveQLAgent}s.
 * @author James MacGlashan
 *
 */
public class SGNaiveQFactory implements AgentFactory {

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
	 * The default Q-value to which Q-values will be initialized
	 */
	protected double													defaultQ;
	
	
	/**
	 * The state hashing factory the Q-learning algorithm will use
	 */
	protected StateHashFactory											stateHash; 
	
	
	/**
	 * The state abstract the Q-learning algorithm will use
	 */
	protected StateAbstraction											storedAbstraction;
	
	
	
	/**
	 * Initializes the factory. No state abstraction is set to be used.
	 * @param domain The stochastic games domain in which the agent will act
	 * @param discount The discount rate the Q-learning algorithm will use
	 * @param learningRate The learning rate the Q-learning algorithm will use
	 * @param defaultQ The default Q-value to which Q-values will be initialized
	 * @param stateHash The state hashing factory the Q-learning algorithm will use
	 */
	public SGNaiveQFactory(SGDomain domain, double discount, double learningRate, double defaultQ, StateHashFactory stateHash) {
		this.domain = domain;
		this.discount = discount;
		this.learningRate = learningRate;
		this.defaultQ = defaultQ;
		this.stateHash = stateHash;
		this.storedAbstraction = null;
	}
	
	
	/**
	 * Initializes the factory. No state abstraction is set to be used.
	 * @param domain The stochastic games domain in which the agent will act
	 * @param discount The discount rate the Q-learning algorithm will use
	 * @param learningRate The learning rate the Q-learning algorithm will use
	 * @param defaultQ The default Q-value to which Q-values will be initialized
	 * @param stateHash The state hashing factory the Q-learning algorithm will use
	 * @param storedAbstraction the state abstraction the Q-learning algorithm will use
	 */
	public SGNaiveQFactory(SGDomain domain, double discount, double learningRate, double defaultQ, StateHashFactory stateHash, StateAbstraction storedAbstraction) {
		this.domain = domain;
		this.discount = discount;
		this.learningRate = learningRate;
		this.defaultQ = defaultQ;
		this.stateHash = stateHash;
		this.storedAbstraction = storedAbstraction;
	}
	
	
	/**
	 * Sets the factory to provide Q-learning algorithms with the given state abstraction.
	 * @param abs the state abstraction to use
	 */
	public void setStoredAbstraction(StateAbstraction abs){
		this.storedAbstraction = abs;
	}

	@Override
	public Agent generateAgent() {
		SGNaiveQLAgent agent = new SGNaiveQLAgent(domain, discount, learningRate, defaultQ, stateHash);
		if(storedAbstraction != null){
			agent.setStoredMapAbstraction(storedAbstraction);
		}
		return agent;
	}

}
