package burlap.behavior.singleagent.planning;

import burlap.behavior.singleagent.MDPSolverInterface;
import burlap.oomdp.core.states.State;

/**
 * @author James MacGlashan.
 */
public interface Planner extends MDPSolverInterface{

	/**
	 * This method will cause the planner to begin planning from the specified initial state
	 * @param initialState the initial state of the planning problem
	 */
	void planFromState(State initialState);

}
