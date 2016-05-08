package burlap.behavior.functionapproximation.dense;

import burlap.mdp.core.AbstractGroundedAction;
import burlap.mdp.core.state.State;

/**
 * @author James MacGlashan.
 */
public interface DenseStateActionFeatures {

	/**
	 * Returns a feature vector represented as a double array for a given input state-action pair.
	 * @param s the input state
	 * @param a the input action
	 * @return the feature vector represented as a double array.
	 */
	double [] features(State s, AbstractGroundedAction a);

	/**
	 * Returns a copy of this {@link DenseStateActionFeatures}
	 * @return a copy of this {@link DenseStateActionFeatures}
	 */
	DenseStateActionFeatures copy();

}
