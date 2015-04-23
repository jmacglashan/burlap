package burlap.oomdp.core.values;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import burlap.oomdp.core.Attribute;


/**
 * A multi-target relational value object subclass. Values are stored as an ordered set (TreeSet) of string names
 * of the object instance name identifiers. If the attribute is not linked to any target, the set will be empty.
 * @author James MacGlashan
 *
 */
public class MultiTargetRelationalValue extends OOMDPValue implements Value{

	/**
	 * The set of object targets to which this value points. Object targets are indicated
	 * by their object name identifier.
	 */
	protected Set <String>		targetObjects;
	
	
	/**
	 * Initializes the value to be associted with the given attribute
	 * @param attribute the attribute with which this value is associated
	 */
	public MultiTargetRelationalValue(Attribute attribute){
		super(attribute);
		this.targetObjects = new TreeSet<String>();
	}
	
	
	/**
	 * Initializes this value as a copy from the source Value object v.
	 * @param v the source Value to make this object a copy of.
	 */
	public MultiTargetRelationalValue(MultiTargetRelationalValue v){
		super(v);
		MultiTargetRelationalValue rv = (MultiTargetRelationalValue)v;
		this.targetObjects = new TreeSet<String>(rv.targetObjects);
	}
	
	
	
	@Override
	public Value copy() {
		return new MultiTargetRelationalValue(this);
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
	public Value setValue(String v) {
		this.targetObjects.clear();
		this.targetObjects.add(v);
		return this;
	}
	
	@Override
	public Value setValue(boolean v) {
		throw new UnsupportedOperationException("Value is of relational; cannot be set to a boolean value.");
	}
	
	@Override
	public Value addRelationalTarget(String t) {
		this.targetObjects.add(t);
		return this;
	}
	
	@Override
	public Value addAllRelationalTargets(Collection<String> targets) {
		this.targetObjects.addAll(targets);
		return this;
	}
	
	
	@Override
	public Value clearRelationTargets() {
		this.targetObjects.clear();
		return this;
	}
	
	@Override
	public Value removeRelationalTarget(String target) {
		this.targetObjects.remove(target);
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
		return this.targetObjects;
	}
	

	@Override
	public StringBuilder buildStringVal(StringBuilder builder) {
		boolean didFirst = false;
		for(String t : this.targetObjects){
			if(didFirst){
				builder.append(";");
			}
			builder.append(t);
			didFirst = true;
		}
		return builder;
	}

	@Override
	public double getNumericRepresentation() {
		return 0;
	}
	
	
	@Override
	public boolean equals(Object obj){
		
		if(!(obj instanceof MultiTargetRelationalValue)){
			return false;
		}
		
		MultiTargetRelationalValue op = (MultiTargetRelationalValue)obj;
		if(!op.attribute.equals(attribute)){
			return false;
		}
		
		if(this.targetObjects.size() != op.targetObjects.size()){
			return false;
		}
		
		for(String t : this.targetObjects){
			if(!op.targetObjects.contains(t)){
				return false;
			}
		}
		
		return true;
		
	}


	@Override
	public boolean getBooleanValue() {
		throw new UnsupportedOperationException("Value is MultiTargetRelational, cannot return boolean representation.");
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
