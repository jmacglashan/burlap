package burlap.oomdp.core.state;

import burlap.oomdp.core.state.annotations.ShallowStateCopy;

import java.util.ArrayList;
import java.util.List;

/**
 * A null state that contains no information.
 */
@ShallowStateCopy
public final class NullState implements State{

	public final static burlap.oomdp.core.state.NullState instance = new burlap.oomdp.core.state.NullState();

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
		return obj instanceof burlap.oomdp.core.state.NullState;
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
