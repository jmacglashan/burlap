package burlap.oomdp.core.values;

import burlap.oomdp.core.Attribute;

public abstract class OOMDPValue implements Value{

	protected Attribute			attribute;			//defines the attribute kind of this value
	protected boolean			isObservable=true;	//relevant to POMDPs for which values are only observable at certain times
	
	
	/**
	 * Initializes this value to be an assignment for Attribute attribute.
	 * @param attribute
	 */
	public OOMDPValue(Attribute attribute){
		this.attribute = attribute;
	}
	
	/**
	 * Initializes this value as a copy from the source Value object v. Should be overridden by subclasses for full copy support.
	 * @param v the source Value to make this object a copy of.
	 */
	public OOMDPValue(OOMDPValue v){
		this.attribute = v.attribute;
	}
	
	/**
	 * Returns the Attribute object for which this is a value assignment.
	 * @return the Attribute object for which this is a value assignment.
	 */
	public Attribute getAttribute(){
		return attribute;
	}
	
	/**
	 * The name of the Attribute object for which this is a value assignment.
	 * @return name of the Attribute object for which this is a value assignment.
	 */
	public String attName(){
		return attribute.name;
	}
	
	
	/**
	 * Sets whether this value is observable to the agent or not.
	 * @param isObservable true if this value is observable to the agent; false otherwise.
	 */
	public OOMDPValue setObservability(boolean isObservable){
		this.isObservable = isObservable;
		return this;
	}
	
	
	/**
	 * Returns whether this value is observable to the agent or not.
	 * @return true if this value is observable to the agent; false otherwise.
	 */
	public boolean isObservable(){
		return this.isObservable;
	}
	
	@Override
	public String toString(){
		return this.getStringVal();
	}

	@Override
	public String getStringVal() {
		return this.buildStringVal(new StringBuilder()).toString();
	}
	
}
