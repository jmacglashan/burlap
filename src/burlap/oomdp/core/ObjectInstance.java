package burlap.oomdp.core;

import java.util.*;

import burlap.oomdp.core.Attribute.AttributeType;


/**
 * Object Instances are the primary element for defining states. An object instance as a name
 * identifier that is unique from any other object instances in the same state. Object Instances
 * belong to a specific object class, and have a set of value assignments for each of its object
 * class' attributes.
 * @author James MacGlashan
 *
 */
public class ObjectInstance {
	
	protected ObjectClass					obClass;			//object class to which this object belongs
	protected String						name;				//name of the object for disambiguation
	protected List <Value>					values;				//the values for each attribute
	
	
	
	
	/**
	 * Initializes an object instance for a given object class and name. 
	 * @param obClass the object class to which this object belongs
	 * @param name the name of the object
	 */
	public ObjectInstance(ObjectClass obClass, String name){
		
		this.obClass = obClass;
		this.name = name;
		
		this.initializeValueObjects();
		
	}
	
	/**
	 * Creates a new object instance that is a deep copy of the specified object instance's values.
	 * The object class and name is a shallow copy.
	 * @param o the source object instance from which this will object will copy.
	 */
	public ObjectInstance(ObjectInstance o){
		
		this.obClass = o.obClass;
		this.name = o.name;
		
		this.values = new ArrayList <Value>(obClass.numAttributes());
		for(Value v : o.values){
			values.add(v.copy());
		}
			
	}
	
	
	/**
	 * Creates and returns a new object instance that is a deep copy of this object instance's values.
	 * @return a new object instance that is a deep copy of this object instance's values (the name and object class reference are a shallow copy)
	 */
	public ObjectInstance copy(){
		return new ObjectInstance(this);
	}
	
	
	
	/**
	 * Creates new value object assignments for each of this object instance class's attributes.
	 */
	public void initializeValueObjects(){
		
		values = new ArrayList <Value>(obClass.numAttributes());
		for(Attribute att : obClass.attributeList){
			values.add(att.valueConstructor());
		}
		
	}
	
	
	/**
	 * Sets the name of this object instance.
	 * @param name the name for this object instance.
	 */
	public void setName(String name){
		this.name = name;
	}
	
	
	/**
	 * Sets the value of the attribute named attName for this object instance.
	 * @param attName the name of the attribute whose value is to be set.
	 * @param v the string rep value to which the attribute of this object instance should be set.
	 */
	public void setValue(String attName, String v){
		int ind = obClass.attributeIndex(attName);
		values.get(ind).setValue(v);
		
	}
	
	
	/**
	 * Sets the value of the attribute named attName for this object instance.
	 * @param attName the name of the attribute whose value is to be set.
	 * @param v the double rep value to which the attribute of this object instance should be set.
	 */
	public void setValue(String attName, double v){
		int ind = obClass.attributeIndex(attName);
		values.get(ind).setValue(v);
		
	}
	
	/**
	 * Sets the value of the attribute named attName for this object instance.
	 * @param attName the name of the attribute whose value is to be set.
	 * @param v the int rep value to which the attribute of this object instance should be set.
	 */
	public void setValue(String attName, int v){
		int ind = obClass.attributeIndex(attName);
		values.get(ind).setValue(v);
		
	}
	
	/**
	 * Sets the value of the attribute named attName for this object instance.
	 * @param attName the name of the attribute whose value is to be set.
	 * @param v the int rep value to which the attribute of this object instance should be set.
	 */
	public void setValue(String attName, boolean v){
		int ind = obClass.attributeIndex(attName);
		values.get(ind).setValue(v);
		
	}
	
	/**
	 * Sets the value of the attribute named attName for this object instance.
	 * @param attName the name of the attribute whose value is to be set.
	 * @param v the int array rep value to which the attribute of this object instance should be set.
	 */
	public void setValue(String attName, int [] v){
		int ind = obClass.attributeIndex(attName);
		values.get(ind).setValue(v);
		
	}
	
	/**
	 * Sets the value of the attribute named attName for this object instance.
	 * @param attName the name of the attribute whose value is to be set.
	 * @param v the double array rep value to which the attribute of this object instance should be set.
	 */
	public void setValue(String attName, double [] v){
		int ind = obClass.attributeIndex(attName);
		values.get(ind).setValue(v);
		
	}
	
	/**
	 * Sets the relational value of the attribute named attName for this object instance. If the
	 * attribute is a multi-target relational attribute, then this value is added to the target list.
	 * @param attName the name of the relational attribute that will have a relational target added/set
	 * @param target the name of the object reference that is to be added as a target.
	 */
	public void addRelationalTarget(String attName, String target){
		int ind = obClass.attributeIndex(attName);
		values.get(ind).addRelationalTarget(target);
	}
	
	/**
	 * Adds all relational targets to the attribute attName for this object instance. For a single-target
	 * relational attribute, the value that is ultimately set depends on the iteration order of iterable. 
	 * @param attName the name of the relational attribute that will have a relational target added/set
	 * @param targets the names of the object references that are to be added as a targets.
	 */
	
	public void addAllRelationalTargets(String attName, Collection<String> targets) {
		int ind = obClass.attributeIndex(attName);
		values.get(ind).addAllRelationalTargets(targets);
	}
	
	/**
	 * Clears all the relational value targets of the attribute named attName for this object instance.
	 * @param attName
	 */
	public void clearRelationalTargets(String attName){
		int ind = obClass.attributeIndex(attName);
		values.get(ind).clearRelationTargets();
	}
	
	/**
	 * Removes an object target from the specified relational attribute.
	 * @param attName the name of the relational attribute from which the target should be removed.
	 * @param target the target to remove from the relational attribute value.
	 */
	public void removeRelationalTarget(String attName, String target){
		int ind = obClass.attributeIndex(attName);
		values.get(ind).removeRelationalTarget(target);
	}
	
	
	/**
	 * Returns the name identifier of this object instance
	 * @return the name identifier of this object instance
	 */
	public String getName(){
		return name;
	}
	
	
	/**
	 * Returns this object instance's object class
	 * @return this object instance's object class
	 */
	public ObjectClass getObjectClass(){
		return obClass;
	}
	
	
	/**
	 * Returns the name of this object instance's object class
	 * @return the name of this object instance's object class
	 */
	public String getTrueClassName(){
		return obClass.name;
	}
	
	
	/**
	 * Returns the Value object assignment for the attribute named attName
	 * @param attName the name of the attribute whose value should be returned
	 * @return the Value object assignment for the attribute named attName
	 */
	public Value getValueForAttribute(String attName){
		int ind = obClass.attributeIndex(attName);
		return values.get(ind);
	}
	
	/**
	 * Returns the double value assignment for the real-valued attribute named attName.
	 * Will throw a runtime exception is the attribute named attName is not of type REAL or REALUNBOUNDED
	 * @param attName the name of the attribute whose value should be returned
	 * @return the double value assignment for the real-valued attribute named attName.
	 */
	public double getRealValForAttribute(String attName){
		int ind = obClass.attributeIndex(attName);
		return values.get(ind).getRealVal();
	}
	
	/**
	 * Returns the double value for the attribute named attType. This method differs from
	 * the {@link #getRealValForAttribute(String)} method because it will cast the int
	 * values for non real attributes to double values and will not throw an exception.
	 * Note that if this method is called on relational attributes, it will return 0.,
	 * where as attributes like {@link AttributeType#INT} and {@link AttributeType#DISC}
	 * will cast their int values to doubles.
	 * @param attName the name of the attribute whose value should be returned
	 * @return a double value assignment for the attribute; casting occurs if the attribute is not real-valued.
	 */
	public double getNumericValForAttribute(String attName){
		int ind = obClass.attributeIndex(attName);
		return values.get(ind).getNumericRepresentation();
	}
	
	/**
	 * Returns the string value representation for the attribute named attName.
	 * @param attName the name of the attribute whose value should be returned
	 * @return the string value assignment for the attribute named attName.
	 */
	public String getStringValForAttribute(String attName){
		int ind = obClass.attributeIndex(attName);
		return values.get(ind).getStringVal();
	}
	
	/**
	 * Returns the int value assignment for the discrete-valued attribute named attName.
	 * Will throw a runtime exception is the attribute named attName is not of type DISC
	 * @param attName the name of the attribute whose value should be returned
	 * @return the int value assignment for the discrete-valued attribute named attName.
	 */
	public int getDiscValForAttribute(String attName){
		int ind = obClass.attributeIndex(attName);
		return values.get(ind).getDiscVal();
	}
	
	
	
	/**
	 * Returns the set of all relational targets to which the relational attribute named attName is set.
	 * If attName is a single target relational attribute, then it will return a set of at most cardinality one.
	 * @param attName attName the name of the attribute whose value should be returned
	 * @return the set of all object instance targets (indicated by their object instance name) for the relational-valued attribute named attName.
	 */
	public Set <String> getAllRelationalTargets(String attName){
		int ind = obClass.attributeIndex(attName);
		return values.get(ind).getAllRelationalTargets();
	}
	
	/**
	 * Returns the boolean value of the attribute (only defined for boolean attributes, int, and disc values).
	 * @param attName the name of the attribute whose value should be returned
	 * @return true if the value for the attribute evaluates to true, false otherwise.
	 */
	public boolean getBooleanValue(String attName){
		int ind = obClass.attributeIndex(attName);
		return values.get(ind).getBooleanValue();
	}
	
	/**
	 * Returns the int array value of the attribute (only defined for int array attributes).
	 * @param attName the name of the attribute whose value should be returned.
	 * @return the int array value.
	 */
	public int [] getIntArrayValue(String attName){
		int ind = obClass.attributeIndex(attName);
		return values.get(ind).getIntArray();
	}
	
	
	/**
	 * Returns the int array value of the attribute (only defined for int array attributes).
	 * @param attName the name of the attribute whose value should be returned.
	 * @return the int array value.
	 */
	public double [] getDoubleArrayValue(String attName){
		int ind = obClass.attributeIndex(attName);
		return values.get(ind).getDoubleArray();
	}
	
	
	/**
	 * Returns the list of value object assignments to all of this object instance's attributes.
	 * @return the list of value object assignments to all of this object instance's attributes.
	 */
	public List <Value> getValues(){
		return this.values;
	}
	
	
	/**
	 * Returns a string representation of this object including its name and value attribute value assignment.
	 * @return a string representation of this object including its name and value attribute value assignment.
	 */
	public String getObjectDescription(){
		
		String desc = name + " (" + this.getTrueClassName() + ")\n";
		for(Value v : values){
			desc = desc + "\t" + v.attName() + ":\t" + v.getStringVal() + "\n";
		}
		
		return desc;
	
	}
	
	
	/**
	 * Returns a double vector of all the observable values in this object instance. Discrete values have
	 * their int stored valued converted to a double for this array. This method will throw a runtime exception
	 * if the object instance includes relational values. This method may be useful if objects need to be indexed
	 * in data structures like kd-trees.
	 * @return a double vector of all the observable values in this object instance.
	 */
	public double[] getObservableFeatureVec(){
		
		double [] obsFeatureVec = new double[obClass.observableAttributeIndices.size()];
		for(int i = 0; i < obsFeatureVec.length; i++){
			int ind = obClass.observableAttributeIndices.get(i);
			obsFeatureVec[i] = values.get(ind).getNumericRepresentation();
		}
		
		return obsFeatureVec;
	}
	
	
	/**
	 * Returns a normalized double vector of all the observable values in this object instance. This method relies on the lowerlims and upperlims 
	 * being set for the corresponding attribute. Furthermore, this method will throw a runtime exception
	 * if the object instance includes attributes that are *not* type REAL or INT.
	 * @return a normalized double vector of all the observable values in this object instance.
	 */
	public double [] getNormalizedObservableFeatureVec(){
		
		double [] obsFeatureVec = new double[obClass.observableAttributeIndices.size()];
		for(int i = 0; i < obsFeatureVec.length; i++){
			int ind = obClass.observableAttributeIndices.get(i);
			Value v = values.get(ind);
			Attribute a = v.getAttribute();
			if(a.type != AttributeType.REAL && a.type != AttributeType.INT){
				throw new RuntimeException("Cannot get a normalized numeric value for attribute " + a.name + " because it is not a REAL or INT type.");
			}
			double dv = values.get(ind).getNumericRepresentation();
			double n = (dv - a.lowerLim) / (a.upperLim - a.lowerLim);
			obsFeatureVec[i] = n;
		}
		
		return obsFeatureVec;
		
	}
	
	
	
	public boolean equals(Object obj){
		ObjectInstance op = (ObjectInstance)obj;
		if(op.name.equals(name))
			return true;
		return false;
	}
	
	
	/**
	 * Returns true if the value assignments in this object instance are the same as they are in the target object instance.
	 * @param obj the object instance against which this object instance should be compared.
	 * @return true if this object instance and obj have identical value assignments; false otherwise.
	 */
	public boolean valueEquals(ObjectInstance obj){
	
		if(!obClass.name.equals(obj.obClass.name)){
			return false;
		}
	
		for(Value v : values){
		
			Value ov = obj.getValueForAttribute(v.attName());
			if(!v.equals(ov)){
				return false;
			}
		
		}
		
		return true;
	
	}
	
	
	public int hashCode(){
		return name.hashCode();
	}
	
	

}
