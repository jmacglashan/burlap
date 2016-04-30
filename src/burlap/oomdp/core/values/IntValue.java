package burlap.oomdp.core.values;

import java.util.Collection;
import java.util.Set;

import burlap.oomdp.core.Attribute;


/**
 * This class provides a value for an integer. The value may be negative or postive and exist in any range.
 * This value object should be preferred for discrete values that span a large numeric range and for which
 * it would be ineffecient to store an array of all possible values in the attribtue. Otherwise the
 * {@link DiscreteValue} class should be preferred.
 * @author James MacGlashan
 *
 */
public class IntValue extends OOMDPValue implements Value {

	/**
	 * The int value
	 */
	protected final int			intVal;
	
	
	/**
	 * Initializes for a given attribute. The default value will be set to 0.
	 * @param attribute
	 */
	public IntValue(Attribute attribute) {
		super(attribute);
		this.intVal = 0;
	}
	
	
	/**
	 * Initializes from an existing IntUnBound value.
	 * @param v the value to copy
	 */
	public IntValue(IntValue v) {
		super(v);
		this.intVal = v.intVal;
	}
	
	public IntValue(Attribute attribute, int intVal) {
		super(attribute);
		this.intVal = intVal;
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
	public Value setValue(int v) {
		return new IntValue(this.attribute, v);
	}

	@Override
	public Value setValue(double v) {
		return new IntValue(this.attribute, (int)v);
	}

	@Override
	public Value setValue(String v) {
		return new IntValue(this.attribute, Integer.parseInt(v));
	}
	
	@Override
	public Value setValue(boolean v) {
		return new IntValue(this.attribute, v ? 1 : 0);
	}

	@Override
	public int getDiscVal() {
		return this.intVal;
	}

	@Override
	public StringBuilder buildStringVal(StringBuilder builder) {
		return builder.append(this.intVal);
	}

	@Override
	public double getNumericRepresentation() {
		return (double)this.intVal;
	}
	
	
	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + intVal;
        return result;
    }


	@Override
    public boolean equals(Object obj){
        if (this == obj) {
            return true;
        }
        
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
}
