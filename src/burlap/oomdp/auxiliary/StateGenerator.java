package burlap.oomdp.auxiliary;

import burlap.oomdp.core.states.State;


/**
 * An interface for generating State objects. This may be useful to define for learning in episodic tasks in which
 * the initial state is drawn from some distribution.
 * @author James MacGlashan
 *
 */
public interface StateGenerator {
	/**
	 * Returns a new state object.
	 * @return a new state object.
	 */
	public State generateState();
}
