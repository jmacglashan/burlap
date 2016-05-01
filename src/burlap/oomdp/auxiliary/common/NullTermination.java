package burlap.oomdp.auxiliary.common;

import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;


/**
 * A terminal state function in which no state is considered a terminal state.
 * @author James MacGlashan
 *
 */
public class NullTermination implements TerminalFunction {


	@Override
	public boolean isTerminal(State s) {
		return false;
	}
	

}
