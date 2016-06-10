package burlap.mdp.core.state;

import burlap.mdp.core.state.annotations.ShallowCopyState;

import java.util.ArrayList;
import java.util.List;

/**
 * A null state that contains no information. Access it via the singleton {@link #instance} field.
 */
@ShallowCopyState
public final class NullState implements State{

	public final static NullState instance = new burlap.mdp.core.state.NullState();

	private NullState(){}

	@Override
	public List<Object> variableKeys() {
		return new ArrayList<Object>();
	}

	@Override
	public Object get(Object variableKey) {
		return null;
	}

	@Override
	public State copy() {
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof burlap.mdp.core.state.NullState;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public String toString() {
		return "";
	}
}
