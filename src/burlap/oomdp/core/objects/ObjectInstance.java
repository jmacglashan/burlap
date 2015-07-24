package burlap.oomdp.core.objects;

import java.util.*;

import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.values.Value;


/**
 * Object Instances are the primary element for defining states. An object instance as a name
 * identifier that is unique from any other object instances in the same state. Object Instances
 * belong to a specific object class, and have a set of value assignments for each of its object
 * class' attributes.
 * @author James MacGlashan
 *
 */
public interface ObjectInstance {
	
	
	/**
	 * Creates and returns a new object instance that is a copy of this object instance's values, if mutable the copy should be deep
	 * @return a new object instance that is a copy of this object instance
	 */
	ObjectInstance copy();

	
	
	/**
	 * Sets the name of this object instance.
	 * @param name the name for this object instance.
	 */
	ObjectInstance setName(String name);
	
	
	/**
	 * Sets the value of the attribute named attName for this object instance.
	 * @param attName the name of the attribute whose value is to be set.
	 * @param v the string rep value to which the attribute of this object instance should be set.
	 */
	ObjectInstance setValue(String attName, String v);
	
	
	/**
	 * Sets the value of the attribute named attName for this object instance.
	 * @param attName the name of the attribute whose value is to be set.
	 * @param v the double rep value to which the attribute of this object instance should be set.
	 */
	ObjectInstance setValue(String attName, double v);
	
	/**
	 * Sets the value of the attribute named attName for this object instance.
	 * @param attName the name of the attribute whose value is to be set.
	 * @param v the int rep value to which the attribute of this object instance should be set.
	 */
	ObjectInstance setValue(String attName, int v);
	
	/**
	 * Sets the value of the attribute named attName for this object instance.
	 * @param attName the name of the attribute whose value is to be set.
	 * @param v the int rep value to which the attribute of this object instance should be set.
	 */
	ObjectInstance setValue(String attName, boolean v);
	
	/**
	 * Sets the value of the attribute named attName for this object instance.
	 * @param attName the name of the attribute whose value is to be set.
	 * @param v the int array rep value to which the attribute of this object instance should be set.
	 */
	ObjectInstance setValue(String attName, int [] v);
	
	/**
	 * Sets the value of the attribute named attName for this object instance.
	 * @param attName the name of the attribute whose value is to be set.
	 * @param v the double array rep value to which the attribute of this object instance should be set.
	 */
	ObjectInstance setValue(String attName, double [] v);
	
	/**
	 * Sets/adds the relational value of the attribute named attName for this object instance. If the
	 * attribute is a multi-target relational attribute, then this value is added to the target list.
	 * @param attName the name of the relational attribute that will have a relational target added/set
	 * @param target the name of the object reference that is to be added as a target.
	 */
	ObjectInstance addRelationalTarget(String attName, String target);
	
	/**
	 * Adds all relational targets to the attribute attName for this object instance. For a single-target
	 * relational attribute, the value that is ultimately set depends on the iteration order of iterable. 
	 * @param attName the name of the relational attribute that will have a relational target added/set
	 * @param targets the names of the object references that are to be added as a targets.
	 */
	
	ObjectInstance addAllRelationalTargets(String attName, Collection<String> targets);
	
	/**
	 * Clears all the relational value targets of the attribute named attName for this object instance.
	 * @param attName
	 */
	ObjectInstance clearRelationalTargets(String attName);
	
	/**
	 * Removes an object target from the specified relational attribute.
	 * @param attName the name of the relational attribute from which the target should be removed.
	 * @param target the target to remove from the relational attribute value.
	 */
	ObjectInstance removeRelationalTarget(String attName, String target);
	
	
	/**
	 * Returns the name identifier of this object instance
	 * @return the name identifier of this object instance
	 */
	String getName();
	
	
	/**
	 * Returns this object instance's object class
	 * @return this object instance's object class
	 */
	ObjectClass getObjectClass();
	
	
	/**
	 * Returns the name of this object instance's object class
	 * @return the name of this object instance's object class
	 */
	String getClassName();
	
	
	/**
	 * Returns the Value object assignment for the attribute named attName
	 * @param attName the name of the attribute whose value should be returned
	 * @return the Value object assignment for the attribute named attName
	 */
	Value getValueForAttribute(String attName);
	
	/**
	 * Returns the double value assignment for the real-valued attribute named attName.
	 * Will throw a runtime exception is the attribute named attName is not of type REAL or REALUNBOUNDED
	 * @param attName the name of the attribute whose value should be returned
	 * @return the double value assignment for the real-valued attribute named attName.
	 */
	double getRealValForAttribute(String attName);
	
	/**
	 * Returns the double value for the attribute named attType. This method differs from
	 * the {@link #getRealValForAttribute(String)} method because it will cast the int
	 * values for non real attributes to double values and will not throw an exception.
	 * Note that if this method is called on relational attributes, it will return 0.,
	 * where as attributes like {@link burlap.oomdp.core.Attribute.AttributeType#INT} and {@link burlap.oomdp.core.Attribute.AttributeType#DISC}
	 * will cast their int values to doubles.
	 * @param attName the name of the attribute whose value should be returned
	 * @return a double value assignment for the attribute; casting occurs if the attribute is not real-valued.
	 */
	double getNumericValForAttribute(String attName);
	
	/**
	 * Returns the string value representation for the attribute named attName.
	 * @param attName the name of the attribute whose value should be returned
	 * @return the string value assignment for the attribute named attName.
	 */
	String getStringValForAttribute(String attName);
	
	/**
	 * Returns the int value assignment for the discrete-valued attribute named attName.
	 * Will throw a runtime exception is the attribute named attName is not of type DISC
	 * @param attName the name of the attribute whose value should be returned
	 * @return the int value assignment for the discrete-valued attribute named attName.
	 */
	int getIntValForAttribute(String attName);
	
	
	
	/**
	 * Returns the set of all relational targets to which the relational attribute named attName is set.
	 * If attName is a single target relational attribute, then it will return a set of at most cardinality one.
	 * @param attName attName the name of the attribute whose value should be returned
	 * @return the set of all object instance targets (indicated by their object instance name) for the relational-valued attribute named attName.
	 */
	Set <String> getAllRelationalTargets(String attName);

	/**
	 * Returns the boolean value of the attribute (only defined for boolean attributes, int, and disc values).
	 * @param attName the name of the attribute whose value should be returned
	 * @return true if the value for the attribute evaluates to true, false otherwise.
	 */
	boolean getBooleanValForAttribute(String attName);
	

	/**
	 * Returns the int array value of the attribute (only defined for int array attributes).
	 * @param attName the name of the attribute whose value should be returned.
	 * @return the int array value.
	 */
	int [] getIntArrayValForAttribute(String attName);
	

	/**
	 * Returns the int array value of the attribute (only defined for int array attributes).
	 * @param attName the name of the attribute whose value should be returned.
	 * @return the int array value.
	 */
	double [] getDoubleArrayValForAttribute(String attName);
	
	
	/**
	 * Returns the list of value object assignments to all of this object instance's attributes.
	 * @return the list of value object assignments to all of this object instance's attributes.
	 */
	List <Value> getValues();


	/**
	 * Returns a list of the names of {@link burlap.oomdp.core.Attribute}s that have unset values
	 * @return a list of the names of attributes that have unset values
	 */
	List<String> unsetAttributes();
	
	
	/**
	 * Returns a string representation of this object including its name and value attribute value assignment.
	 * @return a string representation of this object including its name and value attribute value assignment.
	 */
	String getObjectDescription();

	StringBuilder buildObjectDescription(StringBuilder builder);
	/**
	 * Returns a string description of the object with the unset attribute values listed as null.
	 * @return a string description of the object with the unset attribute values listed as null.
	 */
	String getObjectDescriptionWithNullForUnsetAttributes();
	
	
	/**
	 * Returns a double vector of all the observable values in this object instance. Discrete values have
	 * their int stored valued converted to a double for this array. This method will throw a runtime exception
	 * if the object instance includes relational values. This method may be useful if objects need to be indexed
	 * in data structures like kd-trees.
	 * @return a double vector of all the observable values in this object instance.
	 */
	double[] getFeatureVec();
	
	
	/**
	 * Returns a normalized double vector of all the observable values in this object instance. This method relies on the lowerlims and upperlims 
	 * being set for the corresponding attribute. Furthermore, this method will throw a runtime exception
	 * if the object instance includes attributes that are *not* type REAL or INT.
	 * @return a normalized double vector of all the observable values in this object instance.
	 */
	double [] getNormalizedFeatureVec();
	

	
	
	/**
	 * Returns true if the value assignments in this object instance are the same as they are in the target object instance.
	 * This may differ from a normal equals because it does not require the object instance name identifiers to be the same.
	 * @param obj the object instance against which this object instance should be compared.
	 * @return true if this object instance and obj have identical value assignments; false otherwise.
	 */
	boolean valueEquals(ObjectInstance obj);
	
	
	

}
