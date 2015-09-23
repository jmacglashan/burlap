package burlap.oomdp.singleagent.explorer;

import burlap.oomdp.core.states.State;


/**
 * An interface for defining special non-domain actions to take in a visual explorer.
 * @author James MacGlashan
 *
 */
public interface SpecialExplorerAction {
	public State applySpecialAction(State curState);
}
