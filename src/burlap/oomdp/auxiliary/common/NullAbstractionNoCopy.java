package burlap.oomdp.auxiliary.common;

import burlap.oomdp.auxiliary.StateAbstraction;
import burlap.oomdp.core.states.State;

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
