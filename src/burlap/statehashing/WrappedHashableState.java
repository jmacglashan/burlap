package burlap.statehashing;

import burlap.mdp.core.state.State;

/**
 * @author James MacGlashan.
 */
public abstract class WrappedHashableState implements HashableState {

	/**
	 * The source {@link State} to be hashed and evaluated by the {@link #hashCode()} and {@link #equals(Object)} method.
	 */
	protected State s;

	public WrappedHashableState() {
	}

	public WrappedHashableState(State s) {
		this.s = s;
	}

	@Override
	public State s() {
		return s;
	}


	@Override
	public abstract int hashCode();

	@Override
	public abstract boolean equals(Object obj);

}
