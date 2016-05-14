package burlap.mdp.singleagent.model.statemodel;

import burlap.mdp.core.state.State;

/**
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
