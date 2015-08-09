package burlap.oomdp.stateserialization.simple;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.core.states.State;
import burlap.oomdp.stateserialization.SerializableState;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link burlap.oomdp.stateserialization.SerializableState} representation that reads all {@link burlap.oomdp.core.objects.ObjectInstance} and
 * {@link burlap.oomdp.core.values.Value} objects stored in a {@link burlap.oomdp.core.states.State} and represents their information
 * with {@link burlap.oomdp.stateserialization.simple.SimpleSerializedObjectInstance} and {@link burlap.oomdp.stateserialization.simple.SimpleSerializedValue}
 * instances. Deserialized {@link burlap.oomdp.core.states.State} objects are {@link burlap.oomdp.core.states.MutableState} instances.
 * @author James MacGlashan.
 */
public class SimpleSerializableState extends SerializableState {

	public List<SimpleSerializedObjectInstance> objects;

	public SimpleSerializableState(){

	}

	public SimpleSerializableState(State s) {
		super(s);
	}

	@Override
	public void serialize(State s) {
		List<ObjectInstance> objects = s.getAllObjects();
		this.objects = new ArrayList<SimpleSerializedObjectInstance>(objects.size());
		for(ObjectInstance o : objects){
			this.objects.add(new SimpleSerializedObjectInstance(o));
		}
	}

	@Override
	public State deserialize(Domain domain) {
		State s = new MutableState();
		for(SimpleSerializedObjectInstance o : this.objects){
			s.addObject(o.deserialize(domain));
		}
		return s;
	}


}
