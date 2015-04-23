package burlap.oomdp.stochasticgames.Callables;

import java.util.Map;

import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.Agent;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.parallel.Parallel.ForEachCallable;

public class ObserveOutcomeCallable extends ForEachCallable<Agent, Boolean> {
	
	private final State abstractedCurrent;
	private final JointAction ja;
	private final Map<String, Double> jointReward;
	private final State abstractedPrime; 
	private final Boolean isTerminal;
	
	public ObserveOutcomeCallable(State abstractedCurrent, JointAction ja, Map<String, Double> jointReward, 
			State abstractedPrime, Boolean isTerminal) {
		this.abstractedCurrent = abstractedCurrent;
		this.ja = ja;
		this.jointReward = jointReward;
		this.abstractedPrime = abstractedPrime;
		this.isTerminal = isTerminal;
	}

	@Override
	public ObserveOutcomeCallable copy() {
		return new ObserveOutcomeCallable(abstractedCurrent, ja, jointReward, abstractedPrime, isTerminal);
	}
	
	@Override
	public Boolean perform(Agent agent) {
		agent.observeOutcome(abstractedCurrent, ja, jointReward, abstractedPrime, isTerminal);
		return true;
	}
}
