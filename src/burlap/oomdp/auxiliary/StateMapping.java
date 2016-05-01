package burlap.oomdp.auxiliary;

import burlap.oomdp.core.State;

/**
 * A state mapping interface that maps one state into another state. Can be useful if mapping one state from one domain into a different domain.
 * @author James MacGlashan
 *
 */
public interface StateMapping {
	State mapState(State s);
}
