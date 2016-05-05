package burlap.mdp.auxiliary.common;

import burlap.mdp.auxiliary.StateAbstraction;
import burlap.mdp.core.state.State;

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

	

}
