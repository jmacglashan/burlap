package burlap.oomdp.stateserialization.simple;

import burlap.oomdp.core.states.State;
import burlap.oomdp.stateserialization.SerializableState;
import burlap.oomdp.stateserialization.SerializableStateFactory;

/**
 * A {@link burlap.oomdp.stateserialization.SerializableStateFactory} for {@link burlap.oomdp.stateserialization.simple.SimpleSerializableState} instances.
 * @author James MacGlashan.
 */
public class SimpleSerializableStateFactory implements SerializableStateFactory {
	@Override
	public SerializableState serialize(State s) {
		return new SimpleSerializableState(s);
	}

	@Override
	public Class<?> getGeneratedClass() {
		return SimpleSerializableState.class;
	}
}
