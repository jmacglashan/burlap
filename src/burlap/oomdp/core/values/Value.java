package burlap.oomdp.core.values;

import java.util.Collection;
import java.util.Set;

import burlap.oomdp.core.Attribute;


/**
 * An abstract class for representing a value assignment for an attribute. Different value subclasses will use different internal
 * representations such as an integer for discrete values or a double for real values. Value set/get methods that are not
 * supported by the subclass will throw a runtime exception. 
 * @author James MacGlashan
 *
 */
public interface Value {

	
	
	
	/**
	 * Creates a deep copy of this value object.
	 * @return a deep copy of this value object.
	 */
	Value copy();

	String attName();
	
	Attribute getAttribute();

	/**
	 * Returns whether the value has been set to a meaningful value. Unset values
	 * typically happen when a new object instance has been created by not had its
	 * values set. Leaving values unset will result in exceptions being thrown
	 * to prevent errors in underspecified states.
	 * @return true if the value has been set; false if not.
	 */
	boolean valueHasBeenSet();


	/**
	 * Sets the internal value representation using an int value
	 * @param v the int value assignment
	 */
	Value setValue(int v);
	
	/**
	 * Sets the internal value representation using a double value
	 * @param v the double value assignment
	 */
	Value setValue(double v);
	
	/**
	 * Sets the internal value representation using a string value
	 * @param v the string value assignment
	 */
	Value setValue(String v);
	
	/**
	 * adds a relational target for the object instance named t
	 * @param t the name of the object instance target
	 */
	Value addRelationalTarget(String t);
	
	Value addAllRelationalTargets(Collection<String> targets);
	
	/**
	 * Removes any relational targets for this attribute
	 */
	Value clearRelationTargets();
	
	
	/**
	 * Removes a specific relational target from the relational value in relational attribute. If the relational
	 * attribute does not have this target specified, then nothing happens. This method is primarily useful
	 * for multi-target relational attributes, but if the attribute is a single-target relational attribute
	 * and its one currently set target is the one passed to this method, then this method will clear the
	 * attribute value.
	 * 
	 * @param target the object name identifier to remove
	 */
	Value removeRelationalTarget(String target);
	
	/**
	 * Sets the internalvalue representation using a boolean value
	 * @param v the boolean value
	 */
	Value setValue(boolean v);
	
	/**
	 * Sets the int array value.
	 * @param intArray the int array value to set.
	 */
	Value setValue(int [] intArray);
	
	/**
	 * Sets the double array value.
	 * @param doubleArray the double array value to set.
	 */
	Value setValue(double [] doubleArray);
	
	/**
	 * Returns the discrete integer value of this Value object
	 * @return the discrete integer value of this Value object
	 */
	int getDiscVal();
	
	/**
	 * Returns the real-valued double value of this Value object
	 * @return the real-valued double value of this Value object
	 */
	double getRealVal();
	
	/**
	 * Returns the string value of this Value object
	 * @return the string value of this Value object
	 */
	String getStringVal();
	
	StringBuilder buildStringVal(StringBuilder builder);
	/**
	 * Returns the ordered set of all relational targets of this object. The set will be empty
	 * if the value is not set to any relational targets.
	 * @return the ordered set of all relational targets of this object.
	 */
	Set <String> getAllRelationalTargets();
	
	/**
	 * Returns the boolean value of this attibute. For int values, this means 0 = false and all other values = true.
	 * @return the boolean value of this attribute.
	 */
	boolean getBooleanValue();
	
	
	/**
	 * Returns an int array value
	 * @return an int array value
	 */
	int [] getIntArray();
	
	
	/**
	 * Returns a double array value
	 * @return a double array value.
	 */
	double [] getDoubleArray();
	
	
	/**
	 * Returns a numeric double representation of this value. If the value is discerete, the int
	 * will be type cast as a double.
	 * @return a numeric double representation of this value
	 */
	double getNumericRepresentation();
	
	
}
