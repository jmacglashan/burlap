package burlap.oomdp.core;

import java.util.Set;


/**
 * An abstract class for representing a value assignment for an attribute. Different value subclasses will use different internal
 * representations such as an integer for discrete values or a double for real values. Value set/get methods that are not
 * supported by the subclass will throw a runtime exception. 
 * @author James MacGlashan
 *
 */
public abstract class Value {

	protected Attribute			attribute;			//defines the attribute kind of this value
	protected boolean			isObservable=true;	//relevant to POMDPs for which values are only observable at certain times
	
	
	/**
	 * Initializes this value to be an assignment for Attribute attribute.
	 * @param attribute
	 */
	public Value(Attribute attribute){
		this.attribute = attribute;
	}
	
	/**
	 * Initializes this value as a copy from the source Value object v. Should be overridden by subclasses for full copy support.
	 * @param v the source Value to make this object a copy of.
	 */
	public Value(Value v){
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
	public void setObservability(boolean isObservable){
		this.isObservable = isObservable;
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
	
	
	/**
	 * Creates a deep copy of this value object.
	 * @return a deep copy of this value object.
	 */
	public abstract Value copy();
	
	
	/**
	 * Sets the internal value representation using an int value
	 * @param v the int value assignment
	 */
	public abstract void setValue(int v);
	
	/**
	 * Sets the internal value representation using a double value
	 * @param v the double value assignment
	 */
	public abstract void setValue(double v);
	
	/**
	 * Sets the internal value representation using a string value
	 * @param v the string value assignment
	 */
	public abstract void setValue(String v);
	
	/**
	 * adds a relational target for the object instance named t
	 * @param t the name of the object instance target
	 */
	public abstract void addRelationalTarget(String t);
	
	/**
	 * Removes any relational targets for this attribute
	 */
	public abstract void clearRelationTargets();
	
	
	/**
	 * Removes a specific relational target from the relational value in relational attribute. If the relational
	 * attribute does not have this target specified, then nothing happens. This method is primarily useful
	 * for multi-target relational attributes, but if the attribute is a single-target relational attribute
	 * and its one currently set target is the one passed to this method, then this method will clear the
	 * attribute value.
	 * 
	 * @param target the object name identifier to remove
	 */
	public abstract void removeRelationalTarget(String target);
	
	
	/**
	 * Returns the discrete integer value of this Value object
	 * @return the discrete integer value of this Value object
	 */
	public abstract int getDiscVal();
	
	/**
	 * Returns the real-valued double value of this Value object
	 * @return the real-valued double value of this Value object
	 */
	public abstract double getRealVal();
	
	/**
	 * Returns the string value of this Value object
	 * @return the string value of this Value object
	 */
	public abstract String getStringVal();
	
	/**
	 * Returns the ordered set of all relational targets of this object. The set will be empty
	 * if the value is not set to any relational targets.
	 * @return the ordered set of all relational targets of this object.
	 */
	public abstract Set <String> getAllRelationalTargets();
	
	
	
	/**
	 * Returns a numeric double representation of this value. If the value is discerete, the int
	 * will be type cast as a double.
	 * @return a numeric double representation of this value
	 */
	public abstract double getNumericRepresentation();
	
	
}
