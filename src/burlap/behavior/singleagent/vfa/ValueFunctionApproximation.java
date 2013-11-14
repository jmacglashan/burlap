package burlap.behavior.singleagent.vfa;

import java.util.List;

import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;


/**
 * A general interface for defining state value or Q-value function approximation and interacting with it
 * via gradient descent methods.
 * @author James MacGlashan
 *
 */
public interface ValueFunctionApproximation {

	/**
	 * Returns a state value approximation for the query state.
	 * @param s the query state whose state value should be approximated
	 * @return a state value approximation for the query state.
	 */
	public ApproximationResult getStateValue(State s);
	
	/**
	 * Returns a state-value (e.g., Q-value) approximation for the query state.
	 * @param s the query state of the state-action pair to be approximated
	 * @param gas the query action of the state-action pair to be approximted
	 * @return a state-value approximation for the query state.
	 */
	public List<ActionApproximationResult> getStateActionValues(State s, List <GroundedAction> gas);
	
	
	/**
	 * Returns the function weight gradient of the given approximation result.
	 * @param approximationResult the approximation result whose weight gradient should be returned
	 * @return the function weight gradient of the given approximation result.
	 */
	public WeightGradient getWeightGradient(ApproximationResult approximationResult);
	
	
	
}
