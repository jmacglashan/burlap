package burlap.mdp.auxiliary.common;

import burlap.mdp.auxiliary.StateAbstraction;
import burlap.mdp.core.state.State;


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

}
