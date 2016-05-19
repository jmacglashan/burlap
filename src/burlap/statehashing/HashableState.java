package burlap.statehashing;


import burlap.mdp.core.state.State;


/**
 * This class provides a hash value for {@link State} objects. This is useful for tabular
 * planning and learning algorithms that make use of hash-backed sets or maps for fast retrieval. You can
 * access the state it hashes from the public data member {@link #s}. If the {@link State}
 * delegate {@link #s} is a {@link burlap.statehashing.HashableState} itself, and you wish
 * to get the underlying {@link State}, then you should use the
 * {@link #s()} method, which will recursively descend and return the base source {@link State}.
 * <p>
 * Implementing this class requires implementing
 * the {@link #hashCode()} and {@link #equals(Object)} method.
 * <p>
 * Note that this class implements the {@link State} interface; however,
 * because the purpose of this class is to used with hashed data structures, it is not recommended that
 * you modify the state.
 *
 * @author James MacGlashan
 *
 */
public interface HashableState{

	/**
	 * Returns the underlying source state that is hashed.
	 * @return The underlying source {@link State} that this object hashes and evaluates.
	 */
	State s();

	HashableState copy();


}



