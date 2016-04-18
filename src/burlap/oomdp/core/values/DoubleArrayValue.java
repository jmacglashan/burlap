package burlap.oomdp.core.values;

import java.util.Arrays;

import burlap.oomdp.core.Attribute;


/**
 * This class implements an attribute value that is defined with a double array. In general, it is reccomended that a series of {@link RealValue} attributes
 * is defined instead of using this class, because the series of individual {@link RealValue}s will have better compatibility with existing BURLAP tools and algorithms, 
 * but this value
 * can be used in cases where there is a very large number of double values that have to be stored in each state to cut down on memory overhead.
 * @author James MacGlashan
 *
 */
public class DoubleArrayValue extends OOMDPValue{

	protected final double [] doubleArray;
	
	public DoubleArrayValue(Attribute attribute) {
		super(attribute);
		this.doubleArray = null;
	}
	
	public DoubleArrayValue(DoubleArrayValue v){
		super(v);
		DoubleArrayValue daValue = (DoubleArrayValue)v;
		if(daValue.doubleArray != null) {
			this.doubleArray = daValue.doubleArray.clone();
		} else {
			this.doubleArray = null;
		}
	}
	
	public DoubleArrayValue(Attribute attribute, double[] doubleArray) {
		super(attribute);
		this.doubleArray = doubleArray;
	}

	@Override
	public Value copy() {
		return new DoubleArrayValue(this);
	}

	@Override
	public boolean valueHasBeenSet() {
		return this.doubleArray != null;
	}

	@Override
	public Value setValue(String v) {
		if(v.startsWith("\"") && v.endsWith("\"")){
			v = v.substring(1, v.length());
		}
		String [] comps = v.split(",");
		double[] doubleArray = new double[comps.length];
		for(int i = 0; i < comps.length; i++){
			doubleArray[i] = Double.parseDouble(comps[i]);
		}
		return new DoubleArrayValue(this.attribute, doubleArray);
	}

	@Override
	public StringBuilder buildStringVal(StringBuilder builder) {
		for(int i = 0; i < this.doubleArray.length; i++){
			if(i > 0){
				builder.append(",");
			}
			builder.append(this.doubleArray[i]);
		}
		return builder;
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
	public Value setValue(int[] intArray) {
		
		double[] doubleArray = new double[intArray.length];
		for(int i = 0; i < intArray.length; i++){
			doubleArray[i] = intArray[i];
		}
		return new DoubleArrayValue(this.attribute, doubleArray);
	}

	@Override
	public Value setValue(double[] doubleArray) {
		return new DoubleArrayValue(this.attribute, doubleArray);
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(doubleArray);
        return result;
    }

	@Override
    public boolean equals(Object obj){
        if (this == obj) {
            return true;
        }
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
}
