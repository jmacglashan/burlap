package burlap.behavior.functionapproximation.sparse;

import burlap.mdp.core.state.State;

import java.util.List;


/**
 * An interface for defining a database of state features that can be returned for any given input state or input state-action pair.
 * @author James MacGlashan
 *
 */
public interface SparseStateFeatures {
	
	/**
	 * Returns non-zero state features for a given state.
	 * @param s the state for which features should be returned
	 * @return the features for state s
	 */
	List<StateFeature> features(State s);


	/**
	 * Returns a deep copy of this feature database. If the feature database is dynamic, then changes to one will
	 * not affect the other.
	 * @return a deep copy of this feature database
	 */
	SparseStateFeatures copy();

	/**
	 * Returns the total number of features
	 * @return the total number of features
	 */
	int numFeatures();
}
