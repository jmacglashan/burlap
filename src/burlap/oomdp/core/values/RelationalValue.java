package burlap.oomdp.core.values;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import burlap.oomdp.core.Attribute;


/**
 * A relational valued value subclass in which values are stored as a single String object for the name of the object instance to which it is linked.
 * If the relational value is not linked to any object, then the String value is set to the empty String: "".
 * @author James MacGlashan
 *
 */
public class RelationalValue  extends OOMDPValue implements Value {

	/**
	 * A string representing the object target of this value. Targets are specified by the object name identifier.
	 * If the relational target is unset, then this value will be set to the empty string "", which is the default value.
	 */
	protected String		target = "";
	
	
	/**
	 * Initializes this value to be an assignment for Attribute attribute.
	 * @param attribute
	 */
	public RelationalValue(Attribute attribute){
		super(attribute);
		this.target = "";
	}
	
	
	/**
	 * Initializes this value as a copy from the source Value object v.
	 * @param v the source Value to make this object a copy of.
	 */
	public RelationalValue(RelationalValue v){
		super(v);
		RelationalValue rv = (RelationalValue)v;
		this.target = rv.target;
	}
	
	@Override
	public Value copy() {
		return new RelationalValue(this);
	}

	@Override
	public boolean valueHasBeenSet() {
		return true;
	}

	@Override
	public Value setValue(int v) {
		throw new UnsupportedOperationException(new Error("Cannot set relation value to a value to an int value"));
	}

	@Override
	public Value setValue(double v) {
		throw new UnsupportedOperationException(new Error("Cannot set relation value to a value to a double value"));
	}
	
	@Override
	public Value setValue(boolean v) {
		throw new UnsupportedOperationException("Value is relational; cannot be set to a boolean value.");
	}

	@Override
	public Value setValue(String v) {
		this.target = v;
		return this;
	}
	
	@Override
	public Value addRelationalTarget(String t) {
		this.target = t;
		return this;
	}
	
	@Override
	public Value addAllRelationalTargets(Collection<String> targets) {
		throw new UnsupportedOperationException("Value is relational, cannot add multiple relational targets");
	}
	
	@Override
	public Value clearRelationTargets() {
		this.target = "";
		return this;
	}
	
	@Override
	public Value removeRelationalTarget(String target) {
		if(this.target.equals(target)){
			this.target = "";
		}
		return this;
	}

	@Override
	public int getDiscVal() {
		throw new UnsupportedOperationException(new Error("Value is relational, cannot return discrete value"));
	}

	@Override
	public double getRealVal() {
		throw new UnsupportedOperationException(new Error("Value is relational, cannot return real value"));
	}
	
	@Override
	public Set<String> getAllRelationalTargets() {
		Set <String> res = new TreeSet<String>();
		res.add(this.target);
		return res;
	}

	@Override
	public StringBuilder buildStringVal(StringBuilder builder) {
		return builder.append(this.target);
	}

	@Override
	public double getNumericRepresentation() {
		return 0;
	}

	
	@Override
	public boolean equals(Object obj){
		
		if(!(obj instanceof RelationalValue)){
			return false;
		}
		
		RelationalValue op = (RelationalValue)obj;
		if(!op.attribute.equals(attribute)){
			return false;
		}
		
		return this.target.equals(op.target);
		
	}


	@Override
	public boolean getBooleanValue() {
		throw new UnsupportedOperationException("Value is relational, cannot return boolean representation.");
	}


	@Override
	public Value setValue(int[] intArray) {
		throw new UnsupportedOperationException("Value is relational; cannot be set to an int array.");
	}


	@Override
	public Value setValue(double[] doubleArray) {
		throw new UnsupportedOperationException("Value is relational; cannot be set to a double array.");
	}


	@Override
	public int[] getIntArray() {
		throw new UnsupportedOperationException("Value is relational; cannot return an int array.");
	}


	@Override
	public double[] getDoubleArray() {
		throw new UnsupportedOperationException("Value is relational; cannot return a double array.");
	}


	
	

}
