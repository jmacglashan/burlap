package burlap.mdp.core;

import burlap.mdp.core.state.State;

/**
 * A tuple for a {@link State} and a double specifying the probability of transitioning to that state.
 * @author James MacGlashan.
 */
public class StateTransitionProb {
	public State s;
	public double p;

	public StateTransitionProb() {
	}

	public StateTransitionProb(State s, double p) {
		this.s = s;
		this.p = p;
	}
}
