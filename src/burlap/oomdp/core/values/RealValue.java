package burlap.oomdp.core.values;

import java.util.Set;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Value;

public class RealValue extends Value {
	protected double		realVal;

	public RealValue(Attribute attribute){
		super(attribute);
	}
	
	public RealValue(Value v){
		super(v);
		RealValue rv = (RealValue)v;
		this.realVal = rv.realVal;
	}
	
	public Value copy(){
		return new RealValue(this);
	}
	
	public void setValue(int v){
		this.realVal = (double)v;
	}
	
	public void setValue(double v){
		this.realVal = v;
	}
	
	public void setValue(String v){
		this.realVal = Double.parseDouble(v);
	}
	
	@Override
	public void addRelationalTarget(String t) {
		throw new UnsupportedOperationException(new Error("Value is real, cannot add relational target"));
	}
	
	public int getDiscVal(){
		throw new UnsupportedOperationException(new Error("Value is real, cannot return discrete value"));
	}
	
	public double getRealVal(){
		return this.realVal;
	}
	
	@Override
	public void clearRelationTargets() {
		throw new UnsupportedOperationException(new Error("Value is real, cannot clear relational targets"));
	}
	
	public String getStringVal(){
		return String.valueOf(this.realVal);
	}
	
	@Override
	public Set<String> getAllRelationalTargets() {
		throw new UnsupportedOperationException(new Error("Value is real, cannot return relational values"));
	}
	
	@Override
	public double getNumericRepresentation() {
		return this.realVal;
	}
	
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
