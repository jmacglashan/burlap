package burlap.behavior.singleagent.planning;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.MDPSolverInterface;
import burlap.oomdp.core.state.State;

/**
 * @author James MacGlashan.
 */
public interface Planner extends MDPSolverInterface{

	/**
	 * This method will cause the {@link burlap.behavior.singleagent.planning.Planner} to begin planning from the specified initial {@link State}.
	 * It will then return an appropriate {@link burlap.behavior.policy.Policy} object that captured the planning results.
	 * Note that typically you can use a variety of different {@link burlap.behavior.policy.Policy} objects
	 * in conjunction with this {@link burlap.behavior.singleagent.planning.Planner} to get varying behavior and
	 * the returned {@link burlap.behavior.policy.Policy} is not required to be used.
	 * @param initialState the initial state of the planning problem
	 * @return a {@link burlap.behavior.policy.Policy} that captures the planning results from input {@link State}.
	 */
	Policy planFromState(State initialState);

}
