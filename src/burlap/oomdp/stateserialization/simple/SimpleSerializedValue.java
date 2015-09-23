package burlap.oomdp.stateserialization.simple;

import burlap.oomdp.core.values.Value;

import java.io.Serializable;

/**
 * A serializable representation of {@link burlap.oomdp.core.values.Value} objects.
 * @author James MacGlashan.
 */
public class SimpleSerializedValue implements Serializable{
	public String attribute;
	public String value;

	public SimpleSerializedValue(){

	}

	/**
	 * Creates a serializable representation for the given {@link burlap.oomdp.core.values.Value}
	 * @param oomdpValue the {@link burlap.oomdp.core.values.Value} this object will represent.
	 */
	public SimpleSerializedValue(Value oomdpValue){
		this.attribute = oomdpValue.attName();
		this.value = oomdpValue.getStringVal();
	}
}
