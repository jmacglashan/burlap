package burlap.oomdp.singleagent.explorer;

import burlap.oomdp.core.State;


/**
 * A special non-domain action that causes a visual explorer to rest the state to a specified base state.
 * @author James MacGlashan
 *
 */
public class StateResetSpecialAction implements SpecialExplorerAction {

	State baseState;
	
	/**
	 * Initializes which base state to reset to
	 * @param s the state to reset to when this action is executed
	 */
	public StateResetSpecialAction(State s){
		baseState = s;
	}
	
	/**
	 * Sets the base state to reset to
	 * @param s the state to reset to when this action is executed
	 */
	public void setBase(State s){
		baseState = s;
	}
	
	@Override
	public State applySpecialAction(State curState) {
		return baseState;
	}

}
