package burlap.statehashing;

import burlap.mdp.core.state.State;


/**
 * This interface is to be used by classes that can produce {@link HashableState} objects
 * that provide a hash values for {@link State} objects. This is useful for tabular
 * methods that make use of {@link java.util.HashSet}s or {@link java.util.HashMap}s for fast retrieval.
 * @author James MacGlashan
 *
 */
public interface HashableStateFactory {

	/**
	 * Turns {@link State} s into a {@link burlap.statehashing.HashableState}
	 * @param s the input {@link State} to transform.
	 * @return a {@link burlap.statehashing.HashableState}.
	 */
	HashableState hashState(State s);




	interface OOHashableStateFactory extends HashableStateFactory{

		/**
		 * Returns true if the {@link burlap.statehashing.HashableState} objects returned are object identifier independent; false if they are dependent.
		 * @return true if the {@link burlap.statehashing.HashableState} objects returned are object identifier independent; false if they are dependent.
		 */
		boolean objectIdentifierIndependent();

	}

	
}
