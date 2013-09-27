package burlap.oomdp.auxiliary.common;

import burlap.oomdp.auxiliary.StateAbstraction;
import burlap.oomdp.core.State;

public class NullAbstraction implements StateAbstraction {

	@Override
	public State abstraction(State s) {
		return s.copy();
	}

}
