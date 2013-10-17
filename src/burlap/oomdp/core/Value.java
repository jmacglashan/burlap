package burlap.oomdp.core;

public abstract class Value {

	protected Attribute			attribute;		//defines the attribute kind of this value
	
	
	
	public Value(Attribute attribute){
		this.attribute = attribute;
	}
	
	public Value(Value v){
		this.attribute = v.attribute;
	}
	
	public abstract Value copy();
	
	public Attribute getAttribute(){
		return attribute;
	}
	
	public String attName(){
		return attribute.name;
	}
	
	public abstract void setValue(int v);
	public abstract void setValue(double v);
	public abstract void setValue(String v);
	
	
	/*
	public void setDiscValue(int v){
		discVal = v;
	}
	
	public void setDiscValue(String v){
		int intv = attribute.discValuesHash.get(v);
		discVal = intv;
	}
	
	public void setRealValue(double v){
		realVal = v;
	}
	*/
	
	
	
	public abstract int getDiscVal();
	public abstract double getRealVal();
	public abstract String getStringVal();
	public abstract double getNumericRepresentation();
	
	/*
	public int getDiscreteDimensionality(){
		return attribute.discValues.size();
	}
	*/
	/*
	public boolean equals(Object obj){
		Value op = (Value)obj;
		if(!op.attribute.equals(attribute)){
			return false;
		}
		if(op.attribute.type == AttributeType.DISC){
			return discVal == op.discVal;
		}
		return realVal == op.realVal;
		
		
	}
	*/
	/*
	public int hashCode(){
		return attribute.hashCode();
	}
	*/
	
}
