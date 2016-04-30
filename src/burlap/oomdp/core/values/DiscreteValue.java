package burlap.oomdp.core.values;

import burlap.oomdp.core.Attribute;


/**
 * A discrete value subclass in which discrete values are stored as int values. The int values correspond to the attributes
 * categorical list of discrete values, so the int values should always be &gt;= 0 unless it is unset, which is specified by a value of -1.
 * @author James MacGlashan
 *
 */
public class DiscreteValue extends OOMDPValue implements Value{
	public static final int UNSET = -1;
	/**
	 * The discrete value stored as an integer. If the attribute
	 * defines categorical values with strings, this int value represents
	 * the index in the list of categorical values. The default value
	 * of -1 indicates an unset attribute value.
	 */
	protected final int			discVal;
	
	
	/**
	 * Initializes this value to be an assignment for Attribute attribute.
	 * @param attribute
	 */
	public DiscreteValue(Attribute attribute){
		super(attribute);
		this.discVal = UNSET;
	}
	
	/**
	 * Initializes this value as a copy from the source Value object v.
	 * @param v the source Value to make this object a copy of.
	 */
	public DiscreteValue(DiscreteValue v){
		super(v);
		DiscreteValue dv = v;
		this.discVal = dv.discVal;
	}
	
	public DiscreteValue(Attribute attribute, int discVal) {
		super(attribute);
		this.discVal = discVal;
	}

	@Override
	public boolean valueHasBeenSet() {
		return this.discVal != UNSET;
	}

	@Override
	public Value copy(){
		return new DiscreteValue(this);
	}
	
	@Override
	public Value setValue(int v){
		return new DiscreteValue(this.attribute, v);
	}
	
	@Override
	public Value setValue(double v){
		return new DiscreteValue(this.attribute, (int)v);
	}
	
	@Override
	public Value setValue(boolean v) {
		int intV = v ? 1 : 0;
		return new DiscreteValue(this.attribute, intV);
	}
	
	@Override
	public Value setValue(String v){
		Integer intv = attribute.discValuesHash.get(v);
		if (intv == null) {
			throw new RuntimeException("String value " + v + " is not applicable for attribute " + this.attribute.name);
		}
		return new DiscreteValue(this.attribute, intv);
	}
	
	@Override
	public int getDiscVal(){
		if(this.discVal == -1){
			throw new UnsetValueException();
		}
		return this.discVal;
	}
	
	@Override
	public StringBuilder buildStringVal(StringBuilder builder) {
		if(this.discVal == -1){
			throw new UnsetValueException();
		}
		return builder.append(attribute.discValues.get(discVal));
	}
	
	@Override
	public double getNumericRepresentation() {
		if(this.discVal == -1){
			throw new UnsetValueException();
		}
		return (double)this.discVal;
	}
	
	
	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + discVal;
        return result;
    }

	@Override
    public boolean equals(Object obj){
        if (this == obj) {
            return true;
        }
        
        if(!(obj instanceof DiscreteValue)){
            return false;
        }
        
        DiscreteValue op = (DiscreteValue)obj;
        if(!op.attribute.equals(attribute)){
            return false;
        }
        
        return discVal == op.discVal;
        
    }

	@Override
	public boolean getBooleanValue() {
		return this.discVal != 0;
	}
	

	


	
	
	
}
