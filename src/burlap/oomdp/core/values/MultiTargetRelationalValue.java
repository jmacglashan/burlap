package burlap.oomdp.core.values;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Value;

public class MultiTargetRelationalValue extends Value {

	protected List <String>		targetObjects;
	
	public MultiTargetRelationalValue(Attribute attribute){
		super(attribute);
		this.targetObjects = new ArrayList<String>();
	}
	
	public MultiTargetRelationalValue(Value v){
		super(v);
		MultiTargetRelationalValue rv = (MultiTargetRelationalValue)v;
		this.targetObjects = new ArrayList<String>(rv.targetObjects);
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
	public List<String> getAllRelationalTargets() {
		return this.targetObjects;
	}

	@Override
	public String getStringVal() {
		StringBuffer buf = new StringBuffer();
		for(int i = 0; i < this.targetObjects.size(); i++){
			if(i > 0){
				buf.append(";");
			}
			buf.append(this.targetObjects.get(i));
		}
		return buf.toString();
	}

	@Override
	public double getNumericRepresentation() {
		return 0;
	}

}
