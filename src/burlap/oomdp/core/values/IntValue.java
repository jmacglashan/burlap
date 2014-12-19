package burlap.oomdp.core.values;

import java.util.Collection;
import java.util.Set;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Value;


/**
 * This class provides a value for an integer. The value may be negative or postive and exist in any range.
 * This value object should be preferred for discrete values that span a large numeric range and for which
 * it would be ineffecient to store an array of all possible values in the attribtue. Otherwise the
 * {@link DiscreteValue} class should be preferred.
 * @author James MacGlashan
 *
 */
public class IntValue extends Value {

	/**
	 * The int value
	 */
	protected int			intVal = 0;
	
	
	/**
	 * Initializes for a given attribute. The default value will be set to 0.
	 * @param attribute
	 */
	public IntValue(Attribute attribute) {
		super(attribute);
	}
	
	
	/**
	 * Initializes from an existing IntUnBound value.
	 * @param v the value to copy
	 */
	public IntValue(Value v) {
		super(v);
		this.intVal = ((IntValue)v).intVal;
	}

	@Override
	public boolean valueHasBeenSet() {
		return true;
	}

	@Override
	public Value copy() {
		return new IntValue(this);
	}

	@Override
	public void setValue(int v) {
		this.intVal = v;
	}

	@Override
	public void setValue(double v) {
		this.intVal = (int)v;
	}

	@Override
	public void setValue(String v) {
		this.intVal = Integer.parseInt(v);
	}
	
	@Override
	public void setValue(boolean v) {
		if(v){
			this.intVal = 1;
		}
		else{
			this.intVal = 0;
		}
	}

	@Override
	public void addRelationalTarget(String t) {
		throw new UnsupportedOperationException("Value is Int, cannot add relational target");
	}
	
	@Override
	public void addAllRelationalTargets(Collection<String> targets) {
		throw new UnsupportedOperationException("Value is Int, cannot add relational targets");
	}
	

	@Override
	public void clearRelationTargets() {
		throw new UnsupportedOperationException("Value is Int, cannot clear relational targets");
	}

	@Override
	public void removeRelationalTarget(String target) {
		throw new UnsupportedOperationException("Value is Int, cannot remove relational target");
	}

	@Override
	public int getDiscVal() {
		return this.intVal;
	}

	@Override
	public double getRealVal() {
		throw new UnsupportedOperationException("Value is Int, cannot return real value");
	}

	@Override
	public String getStringVal() {
		return "" + this.intVal;
	}

	@Override
	public Set<String> getAllRelationalTargets() {
		throw new UnsupportedOperationException("Value is Int, cannot return relational values");
	}

	@Override
	public double getNumericRepresentation() {
		return (double)this.intVal;
	}
	
	
	@Override
	public boolean equals(Object obj){
		if(!(obj instanceof IntValue)){
			return false;
		}
		
		IntValue o = (IntValue)obj;
		
		if(!o.attribute.equals(attribute)){
			return false;
		}
		
		return this.intVal == o.intVal;
		
	}


	@Override
	public boolean getBooleanValue() {
		return this.intVal != 0;
	}
	
	@Override
	public void setValue(int[] intArray) {
		throw new UnsupportedOperationException("Value is int; cannot be set to an int array.");
	}


	@Override
	public void setValue(double[] doubleArray) {
		throw new UnsupportedOperationException("Value is int; cannot be set to a double array.");
	}


	@Override
	public int[] getIntArray() {
		throw new UnsupportedOperationException("Value is int; cannot return an int array.");
	}


	@Override
	public double[] getDoubleArray() {
		throw new UnsupportedOperationException("Value is int; cannot return a double array.");
	}

}
