package burlap.oomdp.auxiliary.common;

import burlap.oomdp.auxiliary.StateAbstraction;
import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.Agent;

/**
 * A StateAbstraction class the input state without copying it.
 * @author James MacGlashan
 *
 */
public class NullAbstractionNoCopy implements StateAbstraction{

	@Override
	public State abstraction(State s) {
		return s;
	}

	@Override
	public State abstraction(State s, Agent a) {
		return s;
	}

	

}
