package burlap.oomdp.core.values;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Value;

public class DiscreteValue extends Value{
	protected int			discVal = -1;
	
	
	public DiscreteValue(Attribute attribute){
		super(attribute);
	}
	
	public DiscreteValue(Value v){
		super(v);
		DiscreteValue dv = (DiscreteValue)v;
		this.discVal = dv.discVal;
	}
	
	public Value copy(){
		return new DiscreteValue(this);
	}
	
	public void setValue(int v){
		this.discVal = v;
	}
	
	public void setValue(double v){
		this.discVal = (int)v;
	}
	
	public void setValue(String v){
		int intv = attribute.discValuesHash.get(v);
		discVal = intv;
	}
	
	public int getDiscVal(){
		return this.discVal;
	}
	
	public double getRealVal(){
		throw new UnsupportedOperationException(new Error("Value is discrete, cannot return real value"));
	}
	
	public String getStringVal(){
		return attribute.discValues.get(discVal);
	}
	
	@Override
	public double getNumericRepresentation() {
		return (double)this.discVal;
	}
	
	public boolean equals(Object obj){
		
		if(!(obj instanceof DiscreteValue)){
			return false;
		}
		
		DiscreteValue op = (DiscreteValue)obj;
		if(!op.attribute.equals(attribute)){
			return false;
		}
		
		return discVal == op.discVal;
		
	}

	
	
	
}
