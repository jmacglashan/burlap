package burlap.oomdp.core.values;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Value;

public class RealValue extends Value {
	protected double		realVal;

	public RealValue(Attribute attribute){
		super(attribute);
	}
	
	public RealValue(Value v){
		super(v);
		RealValue dv = (RealValue)v;
		this.realVal = dv.realVal;
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
	
	public int getDiscVal(){
		throw new UnsupportedOperationException(new Error("Value is real, cannot return discrete value"));
	}
	
	public double getRealVal(){
		return this.realVal;
	}
	
	public String getStringVal(){
		return String.valueOf(this.realVal);
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
