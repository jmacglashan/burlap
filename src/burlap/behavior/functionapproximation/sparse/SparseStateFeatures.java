package burlap.behavior.functionapproximation.sparse;

import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.GroundedAction;

import java.util.List;


/**
 * An interface for defining a database of state features that can be returned for any given input state or input state-action pair.
 * @author James MacGlashan
 *
 */
public interface SparseStateFeatures {
	
	/**
	 * Returns non-zero state features for a given state. This method should be implemented if it is to be used with algorithms
	 * that compute V-values from state features or other values that are independent of the actions.
	 * @param s the state for which features should be returned
	 * @return the features for state s
	 */
	List<StateFeature> features(State s);
	
	
	
	/**
	 * Returns non-zero action features for all of provided actions in state s. This method should be implemented if it is to be used 
	 * with algorithms that compute Q-values from action features or other values are the dependent on the actions.
	 * Note that features for different actions should return different feature ids.
	 * @param s the state for which features should be returned
	 * @param actions the action set for which the action features should be returned
	 * @return returns the set set of action features for each action in actions
	 */
	List<ActionFeaturesSet> getActionFeaturesSets(State s, List <GroundedAction> actions);

	
	
	/**
	 * Returns the number of features this database tracks.
	 * @return the number of features this database tracks.
	 */
	int numberOfFeatures();


	/**
	 * Returns a deep copy of this feature database. If the feature database is dynamic, then changes to one will
	 * not affect the other.
	 * @return a deep copy of this feature database
	 */
	SparseStateFeatures copy();
	
	
}
