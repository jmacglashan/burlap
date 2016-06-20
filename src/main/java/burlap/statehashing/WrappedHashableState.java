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


	/**
	 * Getter for Java Bean serialization purposes.
	 * @return the wrapped {@link State}
	 */
	public State getS() {
		return s;
	}

	/**
	 * Setter for Java Bean serialization purposes.
	 * @param s the wrapped {@link State}
	 */
	public void setS(State s) {
		this.s = s;
	}
}
