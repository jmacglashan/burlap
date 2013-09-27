package burlap.oomdp.auxiliary.common;

import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;

public class NullTermination implements TerminalFunction {


	@Override
	public boolean isTerminal(State s) {
		return false;
	}
	

}
