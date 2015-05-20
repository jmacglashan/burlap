package burlap.oomdp.core.values;

import java.util.Collection;
import java.util.Set;

import burlap.oomdp.core.Attribute;


/**
 * This class implements an attribute value that is defined with an int array. In general, it is reccomended that a series of {@link IntValue} attributes
 * is defined instead of using this class, because a series of {@link IntValue}s will have better compatibility with existing BURLAP tools and algorithms, but this class
 * can be used in cases where there is a very large number of int values that have to be stored in each state to cut down on memory overhead.
 * @author James MacGlashan
 *
 */
public class IntArrayValue extends OOMDPValue implements Value {

	protected final int [] intArray;
	
	
	public IntArrayValue(Attribute attribute) {
		super(attribute);
		this.intArray = null;
	}
	
	public IntArrayValue(IntArrayValue v){
		super(v);
		IntArrayValue iaValue  = (IntArrayValue)v;
		if(iaValue.intArray != null){
			this.intArray = iaValue.intArray.clone();
		} else {
			this.intArray = null;
		}
	}
	
	public IntArrayValue(Attribute attribute, int[] intArray) {
		super(attribute);
		this.intArray = intArray;
	}

	@Override
	public Value copy() {
		return new IntArrayValue(this);
	}

	@Override
	public boolean valueHasBeenSet() {
		return this.intArray != null;
	}

	@Override
	public Value setValue(String v) {
		if(v.startsWith("\"") && v.endsWith("\"")){
			v = v.substring(1, v.length());
		}
		String [] comps = v.split(",");
		int[] intArray = new int[comps.length];
		for(int i = 0; i < comps.length; i++){
			intArray[i] = Integer.parseInt(comps[i]);
		}
		return new IntArrayValue(this.attribute, intArray);
	}
	
	@Override
	public StringBuilder buildStringVal(StringBuilder builder) {
		for(int i = 0; i < this.intArray.length; i++){
			if(i > 0){
				builder.append(",");
			}
			builder.append(this.intArray[i]);
		}
		return builder;
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
	public Value setValue(int[] intArray) {
		return new IntArrayValue(this.attribute, intArray);
	}

	@Override
	public Value setValue(double[] doubleArray) {
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

}
