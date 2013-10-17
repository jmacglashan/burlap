package burlap.oomdp.core;

import java.util.List;

public abstract class Value {

	protected Attribute			attribute;		//defines the attribute kind of this value
	
	
	
	public Value(Attribute attribute){
		this.attribute = attribute;
	}
	
	public Value(Value v){
		this.attribute = v.attribute;
	}
	
	
	public Attribute getAttribute(){
		return attribute;
	}
	
	public String attName(){
		return attribute.name;
	}
	
	@Override
	public String toString(){
		return this.getStringVal();
	}
	
	public abstract Value copy();
	
	public abstract void setValue(int v);
	public abstract void setValue(double v);
	public abstract void setValue(String v);
	
	public abstract void addRelationalTarget(String t);
	public abstract void clearRelationTargets();
	
	public abstract int getDiscVal();
	public abstract double getRealVal();
	public abstract String getStringVal();
	public abstract List <String> getAllRelationalTargets();
	
	
	public abstract double getNumericRepresentation();
	
	
}
