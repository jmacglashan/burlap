package burlap.oomdp.core.values;

import java.util.Collection;
import java.util.Set;

import burlap.oomdp.core.Attribute;


/**
 * A real-valued value subclass in which real-values are stored as doubles.
 * @author James MacGlashan
 *
 */
public class RealValue extends OOMDPValue implements Value {
	private static final double UNSET = Double.NaN;
	/**
	 * The real value stored as a double. Default value of NaN indicates that the value is unset
	 */
	protected final double		realVal;

	
	/**
	 * Initializes this value to be an assignment for Attribute attribute.
	 * @param attribute
	 */
	public RealValue(Attribute attribute){
		super(attribute);
		this.realVal = UNSET;
	}
	
	
	/**
	 * Initializes this value as a copy from the source Value object v.
	 * @param v the source Value to make this object a copy of.
	 */
	public RealValue(RealValue v){
		super(v);
		RealValue rv = (RealValue)v;
		this.realVal = rv.realVal;
	}
	
	public RealValue(Attribute attribute, double realVal) {
		super(attribute);
		this.realVal = realVal;
	}
	
	@Override
	public Value copy(){
		return new RealValue(this);
	}


	@Override
	public boolean valueHasBeenSet() {
		return !Double.isNaN(this.realVal);
	}

	@Override
	public Value setValue(int v){
		return new RealValue(this.attribute, v);
	}
	
	@Override
	public Value setValue(double v){
		return new RealValue(this.attribute, v);
	}
	
	@Override
	public Value setValue(String v){
		return new RealValue(this.attribute, Double.parseDouble(v));
	}
	
	@Override
	public double getRealVal(){
		if(Double.isNaN(this.realVal)){
			throw new UnsetValueException();
		}
		return this.realVal;
	}
	
	@Override
	public StringBuilder buildStringVal(StringBuilder builder) {
		if(Double.isNaN(this.realVal)){
			throw new UnsetValueException();
		}
		return builder.append(this.realVal);
	}
	
	@Override
	public double getNumericRepresentation() {
		if(Double.isNaN(this.realVal)){
			throw new UnsetValueException();
		}
		return this.realVal;
	}
	
	@Override
	public boolean equals(Object obj){
		
		if(!(obj instanceof RealValue)){
			return false;
		}
		
		RealValue op = (RealValue)obj;
		if(!op.attribute.equals(attribute)){
			return false;
		}
		
		return realVal == op.realVal;
		
	}
}
