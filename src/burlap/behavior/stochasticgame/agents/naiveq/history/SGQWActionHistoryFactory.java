package burlap.behavior.stochasticgame.agents.naiveq.history;

import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.stochasticgames.Agent;
import burlap.oomdp.stochasticgames.AgentFactory;
import burlap.oomdp.stochasticgames.SGDomain;

public class SGQWActionHistoryFactory implements AgentFactory {

	protected SGDomain													domain;
	
	protected double													discount;
	protected double													learningRate;
	
	protected StateHashFactory											stateHash; 
	
	protected int														historySize;
	protected int														maxPlayers;
	protected ActionIdMap												actionMap;
	
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
