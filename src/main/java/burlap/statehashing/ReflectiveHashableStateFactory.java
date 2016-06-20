package burlap.statehashing;

import burlap.mdp.core.state.State;

/**
 * A HashableState factory to use when the source {@link State} objects by default implement the {@link HashableState}
 * interface. The factory will then simply return that state object, cast as a {@link HashableState} or throw
 * a runtime exception if the state does not implement {@link HashableState}. Note that this means that
 * state equality and hashing will defer to the {@link State} object's implementaiton.
 * @author James MacGlashan.
 */
public class ReflectiveHashableStateFactory implements HashableStateFactory {
	@Override
	public HashableState hashState(State s) {
		if(s instanceof HashableState){
			return (HashableState)s;
		}
		throw new RuntimeException("Reflective Hashable State should only be used with State objects that also already implement HashableState.");
	}
}
