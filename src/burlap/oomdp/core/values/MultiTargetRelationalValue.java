package burlap.oomdp.core.values;

import java.util.Set;
import java.util.TreeSet;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Value;

public class MultiTargetRelationalValue extends Value {

	protected Set <String>		targetObjects;
	
	public MultiTargetRelationalValue(Attribute attribute){
		super(attribute);
		this.targetObjects = new TreeSet<String>();
	}
	
	public MultiTargetRelationalValue(Value v){
		super(v);
		MultiTargetRelationalValue rv = (MultiTargetRelationalValue)v;
		this.targetObjects = new TreeSet<String>(rv.targetObjects);
	}
	
	
	
	@Override
	public Value copy() {
		return new MultiTargetRelationalValue(this);
	}

	@Override
	public void setValue(int v) {
		throw new UnsupportedOperationException(new Error("Cannot set relation value to a value to an int value"));
	}

	@Override
	public void setValue(double v) {
		throw new UnsupportedOperationException(new Error("Cannot set relation value to a value to a double value"));
	}

	@Override
	public void setValue(String v) {
		this.targetObjects.clear();
		this.targetObjects.add(v);
	}
	
	@Override
	public void addRelationalTarget(String t) {
		this.targetObjects.add(t);
	}
	
	@Override
	public void clearRelationTargets() {
		this.targetObjects.clear();
	}

	@Override
	public int getDiscVal() {
		throw new UnsupportedOperationException(new Error("Value is relational, cannot return discrete value"));
	}

	@Override
	public double getRealVal() {
		throw new UnsupportedOperationException(new Error("Value is relational, cannot return real value"));
	}
	
	@Override
	public Set<String> getAllRelationalTargets() {
		return this.targetObjects;
	}

	@Override
	public String getStringVal() {
		StringBuffer buf = new StringBuffer();
		boolean didFirst = false;
		for(String t : this.targetObjects){
			if(!didFirst){
				buf.append(";");
				didFirst = true;
			}
			buf.append(t);
		}
		return buf.toString();
	}

	@Override
	public double getNumericRepresentation() {
		return 0;
	}
	
	
	@Override
	public boolean equals(Object obj){
		
		if(!(obj instanceof MultiTargetRelationalValue)){
			return false;
		}
		
		MultiTargetRelationalValue op = (MultiTargetRelationalValue)obj;
		if(!op.attribute.equals(attribute)){
			return false;
		}
		
		if(this.targetObjects.size() != op.targetObjects.size()){
			return false;
		}
		
		for(String t : this.targetObjects){
			if(!op.targetObjects.contains(t)){
				return false;
			}
		}
		
		return true;
		
	}

}
