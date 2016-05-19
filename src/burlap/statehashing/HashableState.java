package burlap.statehashing;


import burlap.mdp.core.state.State;


/**
 * An interface for an object that computes hash codes and performs equality checks for states. Having a separate object
 * for these operations than the underlying state can be useful since it is often the case that different algorithms may want to pose abstractions
 * on the underlying state or handle equality in different ways than the raw {@link State} implementation.
 * Primarily, this is a marker interface to indicate that the object has implementations of
 * {@link #hashCode()} and {@link #equals(Object)}, but it does also require implementing the
 * {@link #s()} method, which should return the underlying state on which the hash and equals operations of this object are
 * performed.
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



}



