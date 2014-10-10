package burlap.oomdp.core.values;

import java.util.Collection;
import java.util.Set;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Value;


/**
 * This class implements an attribute value that is defined with an int array. In general, it is reccomended that a series of {@link IntValue} attributes
 * is defined instead of using this class, because a series of {@link IntValue}s will have better compatibility with existing BURLAP tools and algorithms, but this class
 * can be used in cases where there is a very large number of int values that have to be stored in each state to cut down on memory overhead.
 * @author James MacGlashan
 *
 */
public class IntArrayValue extends Value {

	protected int [] intArray = null;
	
	
	public IntArrayValue(Attribute attribute) {
		super(attribute);
	}
	
	public IntArrayValue(Value v){
		super(v);
		IntArrayValue iaValue  = (IntArrayValue)v;
		if(iaValue.intArray != null){
			this.intArray = iaValue.intArray.clone();
		}
	}

	@Override
	public Value copy() {
		return new IntArrayValue(this);
	}

	@Override
	public void setValue(int v) {
		throw new UnsupportedOperationException("Value is of type IntArray, cannot set single int value.");
	}

	@Override
	public void setValue(double v) {
		throw new UnsupportedOperationException("Value is of type IntArray, cannot set double value.");
	}

	@Override
	public void setValue(String v) {
		if(v.startsWith("\"") && v.endsWith("\"")){
			v = v.substring(1, v.length());
		}
		String [] comps = v.split(",");
		this.intArray = new int[comps.length];
		for(int i = 0; i < comps.length; i++){
			this.intArray[i] = Integer.parseInt(comps[i]);
		}
	}

	@Override
	public void addRelationalTarget(String t) {
		throw new UnsupportedOperationException("Value is of type IntArray, cannot set relational value.");
	}
	
	@Override
	public void addAllRelationalTargets(Collection<String> targets) {
		throw new UnsupportedOperationException("Value is of type IntArray, cannot add relational targets");
	}
	
	@Override
	public void clearRelationTargets() {
		throw new UnsupportedOperationException("Value is of type IntArray, cannot clear values.");
	}

	@Override
	public void removeRelationalTarget(String target) {
		throw new UnsupportedOperationException("Value is of type IntArray, cannot clear values.");
	}

	@Override
	public int getDiscVal() {
		throw new UnsupportedOperationException("Value is of type IntArray, cannot return disc values");
	}

	@Override
	public double getRealVal() {
		throw new UnsupportedOperationException("Value is of type IntArray, cannot return real values");
	}

	@Override
	public String getStringVal() {
		StringBuffer buf = new StringBuffer();
		for(int i = 0; i < this.intArray.length; i++){
			if(i > 0){
				buf.append(",");
			}
			buf.append(this.intArray[i]);
		}
		return buf.toString();
	}

	@Override
	public Set<String> getAllRelationalTargets() {
		throw new UnsupportedOperationException("Value is of type IntArray, cannot return relational values");
	}

	@Override
	public boolean getBooleanValue() {
		throw new UnsupportedOperationException("Value is of type IntArray, cannot return boolean values");
	}

	@Override
	public double getNumericRepresentation() {
		int sum = 0;
		for(int v : this.intArray){
			sum *= 31;
			sum += v;
		}
		return sum;
	}

	@Override
	public void setValue(int[] intArray) {
		this.intArray = intArray;
	}

	@Override
	public void setValue(double[] doubleArray) {
		throw new UnsupportedOperationException("Cannot set int array value to double array value.");	
	}

	@Override
	public int[] getIntArray() {
		return this.intArray;
	}

	@Override
	public double[] getDoubleArray() {
		double [] doubleArray = new double[this.intArray.length];
		for(int i = 0; i < doubleArray.length; i++){
			doubleArray[i] = this.intArray[i];
		}
		return doubleArray;
	}
	
	@Override
	public boolean equals(Object obj){
		if(!(obj instanceof IntArrayValue)){
			return false;
		}
		
		IntArrayValue o = (IntArrayValue)obj;
		
		if(!o.attribute.equals(attribute)){
			return false;
		}
		
		if(this.intArray.length != o.intArray.length){
			return false;
		}
		
		for(int i = 0; i < this.intArray.length; i++){
			if(this.intArray[i] != o.intArray[i]){
				return false;
			}
		}
		
		return true;
		
	}
	
	@Override
	public void setValue(boolean v) {
		throw new UnsupportedOperationException("Value is of type DoubleArray; cannot be set to a boolean value.");
	}

}
