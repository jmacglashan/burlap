package burlap.behavior.stochasticgame.agents.naiveq;

import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.auxiliary.StateAbstraction;
import burlap.oomdp.stochasticgames.Agent;
import burlap.oomdp.stochasticgames.AgentFactory;
import burlap.oomdp.stochasticgames.SGDomain;

public class SGQFactory implements AgentFactory {

	protected SGDomain													domain;
	
	protected double													discount;
	protected double													learningRate;
	protected double													defaultQ;
	
	protected StateHashFactory											stateHash; 
	
	protected StateAbstraction											storedAbstraction;
	
	
	public SGQFactory(SGDomain domain, double discount, double learningRate, double defaultQ, StateHashFactory stateHash) {
		this.domain = domain;
		this.discount = discount;
		this.learningRate = learningRate;
		this.defaultQ = defaultQ;
		this.stateHash = stateHash;
		this.storedAbstraction = null;
	}
	
	public SGQFactory(SGDomain domain, double discount, double learningRate, double defaultQ, StateHashFactory stateHash, StateAbstraction storedAbstraction) {
		this.domain = domain;
		this.discount = discount;
		this.learningRate = learningRate;
		this.defaultQ = defaultQ;
		this.stateHash = stateHash;
		this.storedAbstraction = storedAbstraction;
	}
	
	public void setStoredAbstraction(StateAbstraction abs){
		this.storedAbstraction = abs;
	}

	@Override
	public Agent generateAgent() {
		SGQLAgent agent = new SGQLAgent(domain, discount, learningRate, defaultQ, stateHash);
		if(storedAbstraction != null){
			agent.setStoredMapAbstraction(storedAbstraction);
		}
		return agent;
	}

}
