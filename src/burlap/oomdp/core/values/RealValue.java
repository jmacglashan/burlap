package burlap.oomdp.core.values;

import java.util.Collection;
import java.util.Set;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Value;


/**
 * A real-valued value subclass in which real-values are stored as doubles.
 * @author James MacGlashan
 *
 */
public class RealValue extends Value {
	
	/**
	 * The real value stored as a double. Default value of NaN indicates that the value is unset
	 */
	protected double		realVal = Double.NaN;

	
	/**
	 * Initializes this value to be an assignment for Attribute attribute.
	 * @param attribute
	 */
	public RealValue(Attribute attribute){
		super(attribute);
	}
	
	
	/**
	 * Initializes this value as a copy from the source Value object v.
	 * @param v the source Value to make this object a copy of.
	 */
	public RealValue(Value v){
		super(v);
		RealValue rv = (RealValue)v;
		this.realVal = rv.realVal;
	}
	
	@Override
	public Value copy(){
		return new RealValue(this);
	}
	
	@Override
	public void setValue(int v){
		this.realVal = (double)v;
	}
	
	@Override
	public void setValue(double v){
		this.realVal = v;
	}
	
	@Override
	public void setValue(String v){
		this.realVal = Double.parseDouble(v);
	}
	
	@Override
	public void setValue(boolean v) {
		throw new UnsupportedOperationException("Value is real; cannot be set to a boolean value.");
	}
	
	@Override
	public void addRelationalTarget(String t) {
		throw new UnsupportedOperationException(new Error("Value is real, cannot add relational target"));
	}
	
	@Override
	public void addAllRelationalTargets(Collection<String> targets) {
		throw new UnsupportedOperationException("Value is real, cannot add relational targets");
	}

	@Override
	public int getDiscVal(){
		throw new UnsupportedOperationException(new Error("Value is real, cannot return discrete value"));
	}
	
	@Override
	public double getRealVal(){
		if(Double.isNaN(this.realVal)){
			throw new UnsetValueException();
		}
		return this.realVal;
	}
	
	@Override
	public void clearRelationTargets() {
		throw new UnsupportedOperationException(new Error("Value is real, cannot clear relational targets"));
	}
	
	@Override
	public void removeRelationalTarget(String target) {
		throw new UnsupportedOperationException(new Error("Value is real, cannot modify relational targets"));
	}
	
	@Override
	public String getStringVal(){
		if(Double.isNaN(this.realVal)){
			throw new UnsetValueException();
		}
		return String.valueOf(this.realVal);
	}
	
	@Override
	public Set<String> getAllRelationalTargets() {
		throw new UnsupportedOperationException(new Error("Value is real, cannot return relational values"));
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


	@Override
	public boolean getBooleanValue() {
		throw new UnsupportedOperationException("Value is Real, cannot return boolean representation.");
	}


	@Override
	public void setValue(int[] intArray) {
		throw new UnsupportedOperationException("Value is real; cannot be set to an int array.");
	}


	@Override
	public void setValue(double[] doubleArray) {
		throw new UnsupportedOperationException("Value is real; cannot be set to a double array.");
	}


	@Override
	public int[] getIntArray() {
		throw new UnsupportedOperationException("Value is real; cannot return an int array.");
	}


	@Override
	public double[] getDoubleArray() {
		throw new UnsupportedOperationException("Value is real; cannot return a double array.");
	}
	
}
