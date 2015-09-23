package burlap.oomdp.stochasticgames.Callables;

import java.util.Map;

import burlap.oomdp.auxiliary.StateAbstraction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.stochasticgames.SGAgent;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.parallel.Parallel.ForEachCallable;

public class ObserveOutcomeCallable extends ForEachCallable<SGAgent, Boolean> {
	
	private final JointAction ja;
	private final Map<String, Double> jointReward;
	private final State currentState;
	private final State nextState;
	private final Boolean isTerminal;
	private final StateAbstraction abstraction;
	
	public ObserveOutcomeCallable(State currentState, JointAction ja, Map<String, Double> jointReward, 
			State nextState, StateAbstraction abstraction, Boolean isTerminal) {
		this.currentState = currentState;
		this.nextState = nextState;
		this.ja = ja;
		this.jointReward = jointReward;
		this.isTerminal = isTerminal;
		this.abstraction = abstraction;
	}

	@Override
	public ObserveOutcomeCallable copy() {
		return new ObserveOutcomeCallable(currentState, ja, jointReward, nextState, abstraction, isTerminal);
	}
	
	@Override
	public Boolean perform(SGAgent agent) {
		State abstractedCurrent = abstraction.abstraction(currentState, agent);
		State abstractedNext = abstraction.abstraction(nextState, agent);
		agent.observeOutcome(abstractedCurrent, ja, jointReward, abstractedNext, isTerminal);
		return true;
	}
}
