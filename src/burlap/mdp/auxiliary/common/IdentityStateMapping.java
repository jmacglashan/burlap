package burlap.mdp.auxiliary.common;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.state.State;


/**
 * A StateAbstraction class that does nothing but returns a copy of input state.
 * @author James MacGlashan
 *
 */
public class IdentityStateMapping implements StateMapping {


	@Override
	public State mapState(State s) {
		return s.copy();
	}

}
