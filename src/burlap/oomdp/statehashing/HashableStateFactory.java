package burlap.oomdp.statehashing;

import burlap.oomdp.core.states.State;


/**
 * This interface is to be used by classes that can produce {@link HashableState} objects
 * that provide a hash values for {@link burlap.oomdp.core.states.State} objects. This is useful for tabular
 * methods that make use of {@link java.util.HashSet}s or {@link java.util.HashMap}s for fast retrieval.
 * @author James MacGlashan
 *
 */
public interface HashableStateFactory {

	public HashableState hashState(State s);
	
}
