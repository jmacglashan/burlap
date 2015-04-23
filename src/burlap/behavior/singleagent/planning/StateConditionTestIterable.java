package burlap.behavior.singleagent.planning;

import burlap.oomdp.core.states.State;


/**
 * An extension of the StateConditionTest that is iterable.
 * @author James MacGlashan
 *
 */
public interface StateConditionTestIterable extends StateConditionTest, Iterable<State> {
	/*
	 * This method is used to set the state context to enumerate over states.
	 * This is useful because typically a state test is independent of other state objects
	 * and calling this method can be used to set the context of those variables and over which to enumerate
	 */
	public void setStateContext(State s); 
}
