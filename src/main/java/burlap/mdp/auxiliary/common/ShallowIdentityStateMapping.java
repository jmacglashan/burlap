package burlap.mdp.auxiliary.common;

import burlap.mdp.auxiliary.StateMapping;
import burlap.mdp.core.state.State;

/**
 * A StateAbstraction class the input state without copying it.
 * @author James MacGlashan
 *
 */
public class ShallowIdentityStateMapping implements StateMapping{

	@Override
	public State mapState(State s) {
		return s;
	}

	

}
