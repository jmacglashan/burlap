package burlap.behavior.statehashing;

import burlap.oomdp.core.states.State;


/**
 * This interface is to be used by classes that can produce {@link StateHashTuple} objects, object
 * that provide a hash values for {@link burlap.oomdp.core.states.State} objects. This is useful for tabular
 * planning and learning algorithms that make use of hash-backed sets or maps for fast retrieval.
 * @author James MacGlashan
 *
 */
public interface StateHashFactory {

	public StateHashTuple hashState(State s);
	
}
