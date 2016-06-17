package burlap.behavior.functionapproximation.sparse;

import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;

import java.util.List;

/**
 * @author James MacGlashan.
 */
public interface SparseStateActionFeatures {

	/**
	 * Returns non-zero state-action features for a given state and action.
	 * @param s the state for which features should be returned
	 * @param a the action for which features should be returned
	 * @return the features for state s
	 */
	List<StateFeature> features(State s, Action a);


	/**
	 * Returns a deep copy of this features function. If the features is dynamic, then changes to one will
	 * not affect the other.
	 * @return a deep copy of this features function
	 */
	SparseStateActionFeatures copy();


	/**
	 * Returns the total number of features
	 * @return the total number of features
	 */
	int numFeatures();

}
