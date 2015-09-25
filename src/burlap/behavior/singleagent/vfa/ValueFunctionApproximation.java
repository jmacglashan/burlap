package burlap.behavior.singleagent.vfa;

import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;

import java.util.List;


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
	ApproximationResult getStateValue(State s);
	
	/**
	 * Returns a state-value (e.g., Q-value) approximation for the query state.
	 * @param s the query state of the state-action pair to be approximated
	 * @param gas the query action of the state-action pair to be approximted
	 * @return a state-value approximation for the query state.
	 */
	List<ActionApproximationResult> getStateActionValues(State s, List <GroundedAction> gas);
	
	
	/**
	 * Returns the function weight gradient of the given approximation result.
	 * @param approximationResult the approximation result whose weight gradient should be returned
	 * @return the function weight gradient of the given approximation result.
	 */
	WeightGradient getWeightGradient(ApproximationResult approximationResult);
	
	
	/**
	 * Resets the weights as is learning had never been performed.
	 */
	void resetWeights();
	
	/**
	 * Sets the weight for a features
	 * @param featureId the feature id whose weight should be set
	 * @param w the weight value to use
	 */
	void setWeight(int featureId, double w);
	
	/**
	 * Returns the FunctionWeight for the given function's feature id.
	 * @param featureId the id of function's feature whose weight is returned.
	 * @return the FunctionWeight for the given function's feature id.
	 */
	FunctionWeight getFunctionWeight(int featureId);
	
	/**
	 * Returns the number of features used in this approximator. Note: if features are dynamically added
	 * with experience, this number may change with subsequent calls.
	 * @return the number of features used in this approximator.
	 */
	int numFeatures();


	/**
	 * Returns a deep copy of this {@link burlap.behavior.singleagent.vfa.ValueFunctionApproximation} such that the returned
	 * copy can be modified independently.
	 * @return a deep copy of this value function.
	 */
	ValueFunctionApproximation copy();
	
	
	
}
