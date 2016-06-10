package burlap.mdp.auxiliary.common;

import burlap.mdp.core.state.State;
import burlap.mdp.core.TerminalFunction;


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
