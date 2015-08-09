package burlap.oomdp.stateserialization.simple;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.values.Value;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A serializable representation of {@link burlap.oomdp.core.objects.ObjectInstance} objects.
 * Deserialization produces {@link burlap.oomdp.core.objects.MutableObjectInstance} objects.
 * @author James MacGlashan.
 */
public class SimpleSerializedObjectInstance implements Serializable{

	public String name;
	public String object_class;
	public List<SimpleSerializedValue> values;

	public SimpleSerializedObjectInstance() {
	}

	/**
	 * Initializes by representing the input {@link burlap.oomdp.core.objects.ObjectInstance}.
	 * @param o the {@link burlap.oomdp.core.objects.ObjectInstance} to represent.
	 */
	public SimpleSerializedObjectInstance(ObjectInstance o){
		this.object_class = o.getClassName();
		this.name = o.getName();
		List<Value> values = o.getValues();
		this.values = new ArrayList<SimpleSerializedValue>(values.size());
		for(Value v : values){
			this.values.add(new SimpleSerializedValue(v));
		}
	}

	/**
	 * Turns this representation into an actual {@link burlap.oomdp.core.objects.ObjectInstance} whose class and attributes
	 * are associated with the input {@link burlap.oomdp.core.Domain}
	 * @param domain the {@link burlap.oomdp.core.Domain} to which the returned {@link burlap.oomdp.core.objects.ObjectInstance} {@link burlap.oomdp.core.ObjectClass} and {@link burlap.oomdp.core.Attribute} refers.
	 * @return a {@link burlap.oomdp.core.objects.MutableObjectInstance}
	 */
	public ObjectInstance deserialize(Domain domain){
		MutableObjectInstance o = new MutableObjectInstance(domain.getObjectClass(this.object_class), this.name);
		for(SimpleSerializedValue v : this.values){
			o.setValue(v.attribute, v.value);
		}
		return o;
	}


}
