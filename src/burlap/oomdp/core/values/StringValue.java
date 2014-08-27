package burlap.oomdp.core.values;

import java.util.Collection;
import java.util.Set;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Value;


/**
 * This class provides a value for a string. 
 * @author Greg Yauney (gyauney)
 *
 */
public class StringValue extends Value {

	/**
	 * The string value
	 */
	protected String			stringVal = "";
	
	
	/**
	 * Initializes for a given attribute. The default value will be set to 0.
	 * @param attribute
	 */
	public StringValue(Attribute attribute) {
		super(attribute);
	}
	
	
	/**
	 * Initializes from an existing value.
	 * @param v the value to copy
	 */
	public StringValue(Value v) {
		super(v);
		this.stringVal = ((StringValue)v).stringVal;
	}

	@Override
	public Value copy() {
		return new StringValue(this);
	}

	@Override
	public void setValue(int v) {
		this.stringVal = Integer.toString(v);
	}
	
	@Override
	public void setValue(double v) {
		this.stringVal = Double.toString(v);
	}
	
	@Override
	public void setValue(String v) {
		this.stringVal = v;
	}
	
	@Override
	public void setValue(boolean v) {
		throw new UnsupportedOperationException("Value is of type String; cannot be set to a boolean value.");
	}

	@Override
	public void addRelationalTarget(String t) {
		throw new UnsupportedOperationException("Value is String, cannot add relational target");
	}

	@Override
	public void addAllRelationalTargets(Collection<String> targets) {
		throw new UnsupportedOperationException("Value is String, cannot add relational targets");
	}
	
	@Override
	public void clearRelationTargets() {
		throw new UnsupportedOperationException("Value is String, cannot clear relational targets");
	}

	@Override
	public void removeRelationalTarget(String target) {
		throw new UnsupportedOperationException("Value is String, cannot remove relational target");
	}

	@Override
	public int getDiscVal() {
		throw new UnsupportedOperationException("Value is String, cannot return int value");
	}

	@Override
	public double getRealVal() {
		throw new UnsupportedOperationException("Value is String, cannot return real value");
	}

	@Override
	public String getStringVal() {
		return this.stringVal;
	}

	@Override
	public Set<String> getAllRelationalTargets() {
		throw new UnsupportedOperationException("Value is String, cannot return relational values");
	}

	@Override
	public double getNumericRepresentation() {
		throw new UnsupportedOperationException("Value is String, cannot return numeric representation");
	}
	
	
	@Override
	public boolean equals(Object obj){
		if(!(obj instanceof StringValue)){
			return false;
		}
		
		StringValue o = (StringValue)obj;
		
		if(!o.attribute.equals(attribute)){
			return false;
		}
		
		return this.stringVal.equals(o.stringVal);
		
	}


	@Override
	public boolean getBooleanValue() {
		throw new UnsupportedOperationException("Value is String, cannot return boolean representation.");
	}
	
	@Override
	public void setValue(int[] intArray) {
		throw new UnsupportedOperationException("Value is string; cannot be set to an int array.");
	}


	@Override
	public void setValue(double[] doubleArray) {
		throw new UnsupportedOperationException("Value is string; cannot be set to a double array.");
	}


	@Override
	public int[] getIntArray() {
		throw new UnsupportedOperationException("Value is string; cannot return an int array.");
	}


	@Override
	public double[] getDoubleArray() {
		throw new UnsupportedOperationException("Value is string; cannot return a double array.");
	}

}
