package burlap.oomdp.stateserialization;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.states.State;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A class for transforming {@link burlap.oomdp.core.states.State} objects into
 * an object that can be trivially serialized by using the {@link #serialize(burlap.oomdp.core.states.State)}
 * method. This class does not implement the
 * {@link burlap.oomdp.core.states.State} interface, because the serializable states do not have to reference specific
 * {@link burlap.oomdp.core.Domain} instances or {@link burlap.oomdp.core.ObjectClass} for easy file portability.
 * Instead, a {@link SerializableState} is deserialized into an actual {@link burlap.oomdp.core.states.State}
 * object by calling the {@link #deserialize(burlap.oomdp.core.Domain)} method, which takes the {@link burlap.oomdp.core.Domain}
 * into which the state should be unpacked. There is also a static method {@link #deserializeStates(java.util.List, burlap.oomdp.core.Domain)}
 * which takes a {@link java.util.List} of {@link SerializableState} objects and turns it
 * into a {@link java.util.List} of {@link burlap.oomdp.core.states.State} objects by calling the {@link #deserialize(burlap.oomdp.core.Domain)}
 * method of each element.
 * <br/><br/>
 * In general, {@link SerializableState} objects should be constructed with a
 * {@link SerializableStateFactory}.
 * <br/><br/>
 * A standard serializable representation that operates reading the {@link burlap.oomdp.core.objects.ObjectInstance}
 * and {@link burlap.oomdp.core.values.Value} objects returned by a {@link burlap.oomdp.core.states.State} and explicitly
 * representing their information is the
 * {@link burlap.oomdp.stateserialization.simple.SimpleSerializableState} and its corresponding
 * {@link burlap.oomdp.stateserialization.simple.SimpleSerializableStateFactory}.
 *
 * @author James MacGlashan.
 */
public abstract class SerializableState implements Serializable{

	public SerializableState(){
		//do nothing
	}

	/**
	 * Constructs and serializes the input {@link burlap.oomdp.core.states.State} by calling this object's
	 * {@link #serialize(burlap.oomdp.core.states.State)} method.
	 * @param s the input {@link burlap.oomdp.core.states.State} to serialize.
	 */
	public SerializableState(State s){
		this.serialize(s);
	}


	/**
	 * Causes this object to be a serializable representation of the input {@link burlap.oomdp.core.states.State}
	 * @param s the {@link burlap.oomdp.core.states.State} to be represented by this object.
	 */
	public abstract void serialize(State s);


	/**
	 * Unpacks this {@link burlap.oomdp.stateserialization.SerializableState} into an actual {@link burlap.oomdp.core.states.State}
	 * in which the {@link burlap.oomdp.core.ObjectClass}, {@link burlap.oomdp.core.Attribute} and other {@link burlap.oomdp.core.Domain}
	 * information refers to the provided {@link burlap.oomdp.core.Domain}.
	 * @param domain The {@link burlap.oomdp.core.Domain} specifying {@link burlap.oomdp.core.ObjectClass} and {@link burlap.oomdp.core.Attribute} information.
	 * @return a {@link burlap.oomdp.core.states.State} corresponding to this {@link burlap.oomdp.stateserialization.SerializableState}
	 */
	public abstract State deserialize(Domain domain);


	/**
	 * Takes a {@link java.util.List} of {@link burlap.oomdp.stateserialization.SerializableState} objects and calls the {@link #deserialize(burlap.oomdp.core.Domain)}
	 * method on them to turn it into a {@link java.util.List} of {@link burlap.oomdp.core.states.State} objects.
	 * @param serializableStates the {@link java.util.List} of {@link burlap.oomdp.stateserialization.SerializableState} instances to deserialize.
	 * @param domain the {@link burlap.oomdp.core.Domain} to which all produced {@link burlap.oomdp.core.states.State} objects will be associated.
	 * @return a {@link java.util.List} of {@link burlap.oomdp.core.states.State} objects.
	 */
	public static List<State> deserializeStates(List<SerializableState> serializableStates, Domain domain){
		List<State> states = new ArrayList<State>(serializableStates.size());
		for(SerializableState s : serializableStates){
			states.add(s.deserialize(domain));
		}
		return states;
	}

}
