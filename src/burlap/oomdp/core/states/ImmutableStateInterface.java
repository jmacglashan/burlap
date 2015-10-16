package burlap.oomdp.core.states;

import java.util.List;

import burlap.oomdp.core.objects.ImmutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;

import com.google.common.collect.ImmutableList;

/**
 * A state that implements this interface implies that it itself is immutable and the ObjectInstances it uses are immutable. 
 * An immutable state is one that doesn't allow you to make modifications to its underlying data
 * structure, by using the getObject, setValue paradigm. Any changes to a state will result in a copy that reflects 
 * those changes. The original state will not be modified.
 * @author Stephen Brawner
 *
 */
public interface ImmutableStateInterface extends State, Iterable<ImmutableObjectInstance> {
	ImmutableStateInterface replaceAndHash(ImmutableList<ImmutableObjectInstance> objects, int code);
	ImmutableStateInterface replaceObject(ObjectInstance objectToReplace, ObjectInstance newObject);
	ImmutableStateInterface replaceAllObjects(List<ImmutableObjectInstance> objectsToRemove, List<ImmutableObjectInstance> objectsToAdd);
	ImmutableList<ImmutableObjectInstance> getImmutableObjects();
	boolean isHashed();
}
