package burlap.oomdp.core;

import java.util.Set;

public abstract class Value {

	protected Attribute			attribute;			//defines the attribute kind of this value
	protected boolean			isObservable=true;	//relevant to POMDPs for which values are only observable at certain times
	
	
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
	
	public void setObservability(boolean isObservable){
		this.isObservable = isObservable;
	}
	
	public boolean isObservable(){
		return this.isObservable;
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
	public abstract Set <String> getAllRelationalTargets();
	
	
	public abstract double getNumericRepresentation();
	
	
}
