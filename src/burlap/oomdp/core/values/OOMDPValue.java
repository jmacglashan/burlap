package burlap.oomdp.core.values;

import java.util.Collection;
import java.util.Set;

import burlap.oomdp.core.Attribute;

public abstract class OOMDPValue implements Value{

	protected Attribute			attribute;			//defines the attribute kind of this value
	
	
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
	

	
	@Override
	public String toString(){
		return this.getStringVal();
	}

	@Override
	public String getStringVal() {
		return this.buildStringVal(new StringBuilder()).toString();
	}
	
	/**
	 * Returns the discrete integer value of this Value object
	 * @return the discrete integer value of this Value object
	 */
	public int getDiscVal() {
		throw new UnsupportedOperationException("Value is of type" + this.getClass().getSimpleName() + ", cannot get value as discrete.");
	}
	
	/**
	 * Returns the real-valued double value of this Value object
	 * @return the real-valued double value of this Value object
	 */
	public double getRealVal() {
		throw new UnsupportedOperationException("Value is of type" + this.getClass().getSimpleName() + ", cannot get real value.");
	}
	
	
	public Set <String> getAllRelationalTargets() {
		throw new UnsupportedOperationException("Value is of type" + this.getClass().getSimpleName() + ", cannot get relational targets.");
	}
	
	/**
	 * Returns the boolean value of this attibute. For int values, this means 0 = false and all other values = true.
	 * @return the boolean value of this attribute.
	 */
	public boolean getBooleanValue() {
		throw new UnsupportedOperationException("Value is of type" + this.getClass().getSimpleName() + ", cannot get boolean value.");
	}
	
	
	/**
	 * Returns an int array value
	 * @return an int array value
	 */
	public int [] getIntArray() {
		throw new UnsupportedOperationException("Value is of type" + this.getClass().getSimpleName() + ", cannot get int[] array.");
	}
	
	
	/**
	 * Returns a double array value
	 * @return a double array value.
	 */
	public double [] getDoubleArray() {
		throw new UnsupportedOperationException("Value is of type" + this.getClass().getSimpleName() + ", cannot get double[] array.");
	}
	
	/**
	 * Returns a numeric representation
	 * @return a double value.
	 */
	public double getNumericRepresentation() {
		throw new UnsupportedOperationException("Value is of type" + this.getClass().getSimpleName() + ", cannot get numeric value.");
	}
	
	/**
	 * Sets the internal value representation using an int value
	 * @param v the int value assignment
	 */
	public Value setValue(int v) {
		throw new UnsupportedOperationException("Value is of type" + this.getClass().getSimpleName() + ", cannot set value to int.");
	}
	
	/**
	 * Sets the internal value representation using a double value
	 * @param v the double value assignment
	 */
	public Value setValue(double v) {
		throw new UnsupportedOperationException("Value is of type" + this.getClass().getSimpleName() + ", cannot set value to double.");
	}
	
	/**
	 * Sets the internal value representation using a string value
	 * @param v the string value assignment
	 */
	public Value setValue(String v) {
		throw new UnsupportedOperationException("Value is of type" + this.getClass().getSimpleName() + ", cannot set value to String.");
	}
	
	/**
	 * adds a relational target for the object instance named t
	 * @param t the name of the object instance target
	 */
	public Value addRelationalTarget(String t) {
		throw new UnsupportedOperationException("Value is of type" + this.getClass().getSimpleName() + ", cannot add relational target.");
	}
	
	public Value addAllRelationalTargets(Collection<String> targets) {
		throw new UnsupportedOperationException("Value is of type" + this.getClass().getSimpleName() + ", cannot add relational targets.");
	}
	
	/**
	 * Removes any relational targets for this attribute
	 */
	public Value clearRelationTargets() {
		throw new UnsupportedOperationException("Value is of type" + this.getClass().getSimpleName() + ", cannot remove relational targets.");
	}
	
	
	/**
	 * Removes a specific relational target from the relational value in relational attribute. If the relational
	 * attribute does not have this target specified, then nothing happens. This method is primarily useful
	 * for multi-target relational attributes, but if the attribute is a single-target relational attribute
	 * and its one currently set target is the one passed to this method, then this method will clear the
	 * attribute value.
	 * 
	 * @param target the object name identifier to remove
	 */
	public Value removeRelationalTarget(String target) {
		throw new UnsupportedOperationException("Value is of type" + this.getClass().getSimpleName() + ", cannot remove a relational target.");
	}
	
	/**
	 * Sets the internalvalue representation using a boolean value
	 * @param v the boolean value
	 */
	public Value setValue(boolean v) {
		throw new UnsupportedOperationException("Value is of type" + this.getClass().getSimpleName() + ", cannot set to boolean.");
	}
	
	/**
	 * Sets the int array value.
	 * @param intArray the int array value to set.
	 */
	public Value setValue(int [] intArray) {
		throw new UnsupportedOperationException("Value is of type" + this.getClass().getSimpleName() + ", cannot set int[] array.");
	}
	
	/**
	 * Sets the double array value.
	 * @param doubleArray the double array value to set.
	 */
	public Value setValue(double [] doubleArray) {
		throw new UnsupportedOperationException("Value is of type" + this.getClass().getSimpleName() + ", cannot set double[] array.");
	}
	
}
