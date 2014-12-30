package burlap.oomdp.core.values;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Value;


/**
 * A multi-target relational value object subclass. Values are stored as an ordered set (TreeSet) of string names
 * of the object instance name identifiers. If the attribute is not linked to any target, the set will be empty.
 * @author James MacGlashan
 *
 */
public class MultiTargetRelationalValue extends Value {

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
	public MultiTargetRelationalValue(Value v){
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
	public void setValue(int v) {
		throw new UnsupportedOperationException(new Error("Cannot set relation value to a value to an int value"));
	}

	@Override
	public void setValue(double v) {
		throw new UnsupportedOperationException(new Error("Cannot set relation value to a value to a double value"));
	}

	@Override
	public void setValue(String v) {
		this.targetObjects.clear();
		this.targetObjects.add(v);
	}
	
	@Override
	public void setValue(boolean v) {
		throw new UnsupportedOperationException("Value is of relational; cannot be set to a boolean value.");
	}
	
	@Override
	public void addRelationalTarget(String t) {
		this.targetObjects.add(t);
	}
	
	@Override
	public void addAllRelationalTargets(Collection<String> targets) {
		this.targetObjects.addAll(targets);
	}
	
	
	@Override
	public void clearRelationTargets() {
		this.targetObjects.clear();
	}
	
	@Override
	public void removeRelationalTarget(String target) {
		this.targetObjects.remove(target);
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
	public String getStringVal() {
		StringBuffer buf = new StringBuffer();
		boolean didFirst = false;
		for(String t : this.targetObjects){
			if(didFirst){
				buf.append(";");
			}
			buf.append(t);
			didFirst = true;
		}
		return buf.toString();
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
	public void setValue(int[] intArray) {
		throw new UnsupportedOperationException("Value is relational; cannot be set to an int array.");
	}


	@Override
	public void setValue(double[] doubleArray) {
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
