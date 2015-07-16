package burlap.oomdp.auxiliary.common;

import burlap.oomdp.auxiliary.StateAbstraction;
import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.Agent;


/**
 * A StateAbstraction class that does nothing but returns a copy of input state.
 * @author James MacGlashan
 *
 */
public class NullAbstraction implements StateAbstraction {

	@Override
	public State abstraction(State s) {
		return s.copy();
	}

	@Override
	public State abstraction(State s, Agent a) {
		return s.copy();
	}

}
