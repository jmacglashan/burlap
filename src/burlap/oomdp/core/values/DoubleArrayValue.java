package burlap.oomdp.core.values;

import java.util.Collection;
import java.util.Set;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Value;


/**
 * This class implements an attribute value that is defined with a double array. In general, it is reccomended that a series of {@link RealValue} attributes
 * is defined instead of using this class, because the series of individual {@link RealValue}s will have better compatibility with existing BURLAP tools and algorithms, 
 * but this value
 * can be used in cases where there is a very large number of double values that have to be stored in each state to cut down on memory overhead.
 * @author James MacGlashan
 *
 */
public class DoubleArrayValue extends Value{

	protected double [] doubleArray;
	
	public DoubleArrayValue(Attribute attribute) {
		super(attribute);
	}
	
	public DoubleArrayValue(Value v){
		super(v);
		DoubleArrayValue daValue = (DoubleArrayValue)v;
		if(daValue != null) {
			this.doubleArray = daValue.doubleArray.clone();
		}
	}

	@Override
	public Value copy() {
		return new DoubleArrayValue(this);
	}

	@Override
	public void setValue(int v) {
		throw new UnsupportedOperationException("Value is of type DoubleArray, cannot set value to int.");
	}

	@Override
	public void setValue(double v) {
		throw new UnsupportedOperationException("Value is of type DoubleArray, cannot set value to a single double.");
	}

	@Override
	public void setValue(String v) {
		if(v.startsWith("\"") && v.endsWith("\"")){
			v = v.substring(1, v.length());
		}
		String [] comps = v.split(",");
		this.doubleArray = new double[comps.length];
		for(int i = 0; i < comps.length; i++){
			this.doubleArray[i] = Double.parseDouble(comps[i]);
		}
	}

	@Override
	public void addRelationalTarget(String t) {
		throw new UnsupportedOperationException("Value is of type DoubleArray, cannot set relational value.");
	}

	@Override
	public void addAllRelationalTargets(Collection<String> targets) {
		throw new UnsupportedOperationException("Value is of type DoubleArray, cannot add relational targets");
	}
	
	@Override
	public void clearRelationTargets() {
		throw new UnsupportedOperationException("Value is of type DoubleArray, cannot remove relational value.");
	}

	@Override
	public void removeRelationalTarget(String target) {
		throw new UnsupportedOperationException("Value is of type DoubleArray, cannot remove relational value.");
	}

	@Override
	public int getDiscVal() {
		throw new UnsupportedOperationException("Value is of type DoubleArray, cannot return int value.");
	}

	@Override
	public double getRealVal() {
		throw new UnsupportedOperationException("Value is of type DoubleArray, cannot return single real value.");
	}

	@Override
	public String getStringVal() {
		StringBuffer buf = new StringBuffer();
		for(int i = 0; i < this.doubleArray.length; i++){
			if(i > 0){
				buf.append(",");
			}
			buf.append(this.doubleArray[i]);
		}
		return buf.toString();
	}

	@Override
	public Set<String> getAllRelationalTargets() {
		throw new UnsupportedOperationException("Value is of type DoubleArray, cannot return relational values.");
	}

	@Override
	public boolean getBooleanValue() {
		throw new UnsupportedOperationException("Value is of type DoubleArray, cannot return boolean value.");
	}

	@Override
	public double getNumericRepresentation() {
		double sum = 0;
		for(double v : this.doubleArray){
			sum *= 31;
			sum += v;
		}
		return sum;
	}

	@Override
	public void setValue(int[] intArray) {
		this.doubleArray = new double[intArray.length];
		for(int i = 0; i < intArray.length; i++){
			this.doubleArray[i] = intArray[i];
		}
	}

	@Override
	public void setValue(double[] doubleArray) {
		this.doubleArray = doubleArray;
	}

	@Override
	public int[] getIntArray() {
		if(this.doubleArray == null){
			throw new RuntimeException("Error, double array value is unset, cannot return a value for it.");
		}
		int [] intArray = new int[this.doubleArray.length];
		for(int i = 0; i < this.doubleArray.length; i++){
			intArray[i] = (int)this.doubleArray[i];
		}
		return intArray;
	}

	@Override
	public double[] getDoubleArray() {
		if(this.doubleArray == null){
			throw new RuntimeException("Error, double array value is unset, cannot return a value for it.");
		}
		return this.doubleArray;
	}
	
	
	@Override
	public boolean equals(Object obj){
		if(!(obj instanceof DoubleArrayValue)){
			return false;
		}
		
		DoubleArrayValue o = (DoubleArrayValue)obj;
		
		if(!o.attribute.equals(this.attribute)){
			return false;
		}
		
		if(this.doubleArray.length != o.doubleArray.length){
			return false;
		}
		
		for(int i = 0; i < this.doubleArray.length; i++){
			if(this.doubleArray[i] != o.doubleArray[i]){
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
