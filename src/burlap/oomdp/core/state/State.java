package burlap.oomdp.core.state;

import java.util.ArrayList;
import java.util.List;

/**
 * A State instance is used to define the state of an environment or an observation from the environment. Creating
 * a state requires implementing three methods to ensure compatibility with all BURLAP tools. First, the
 * {@link #variableKeys()} method should return a list of objects that represent the possible keys that are used
 * to reference state variables in your state. In general, a state variable is any value that when changed alters
 * the identification of the state. That is, two states with different state variables would not be equal and transition
 * dynamics and reward functions from them may be different as a result.
 * <br><br>
 * Next the {@link #get(Object)} method should accept any argument that is listed in the {@link #variableKeys()} list
 * and should return the value of that variable. You do *not* have to guarantee that changes the client makes to the
 * returned value will affect the state. In fact, in general, clients should only modify state objects if they
 * implement the MutableState interface through its set method.
 * <br><br>
 * Finally, the {@link #copy()} method must be implemented so that a duplicate copy of this state. If the state
 * implements MutableState, then a copied state should have the property that changes made to copy do
 * not affect the values of the source state, so make sure copies are as deep as necessary.
 *
 * @author James MacGlashan
 *
 */
public interface State {


	/**
	 * Returns the list of state variable keys.
	 * @return the list of state variable keys.
	 */
	List<Object> variableKeys();

	/**
	 * Returns the value for the given variable key. Changes to the returned value are not guaranteed to modify
	 * the state.
	 * @param variableKey the variable key
	 * @return the value for the given variable key
	 */
	Object get(Object variableKey);

	/**
	 * Returns a copy of this state, if mutable the copy should be deep enough that changes to the copied state
	 * do not affect this state.
	 * @return a copy of this state.
	 */
	State copy();


	/**
	 * A null state that contains no information.
	 */
	class NullState implements State{

		public final static NullState instance = new NullState();

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
			return obj instanceof NullState;
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
		
}
