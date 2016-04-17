package burlap.oomdp.core.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Attribute.AttributeType;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.values.Value;

import com.google.common.collect.ImmutableList;


/**
 * Object Instances are the primary element for defining states. An object instance as a name
 * identifier that is unique from any other object instances in the same state. Object Instances
 * belong to a specific object class, and have a set of value assignments for each of its object
 * class' attributes.
 * @author James MacGlashan
 *
 */
public final class ImmutableObjectInstance extends OOMDPObjectInstance implements ObjectInstance, Iterable<Value> {
	
	private final ObjectClass					obClass;			//object class to which this object belongs
	private final String						name;				//name of the object for disambiguation
	private final ImmutableList <Value>			values;				//the values for each attribute
	private final int							hashCode;
	private final boolean 						identifierIndependent;
	
	/**
	 * Initializes an object instance for a given object class and name. 
	 * @param obClass the object class to which this object belongs
	 * @param name the name of the object
	 */
	public ImmutableObjectInstance(ObjectClass obClass, String name){
		
		this.obClass = obClass;
		this.name = name;
		this.values = ImmutableList.copyOf(this.initializeValueObjects());
		this.hashCode = 0;
		this.identifierIndependent = false;
	}
	
	/**
	 * Creates a new object instance that is a shallow copy of the specified object instance's values.
	 * The object class and name is a shallow copy.
	 * @param o the source object instance from which this will object will copy.
	 */
	public ImmutableObjectInstance(ObjectInstance o){
		
		this.obClass = o.getObjectClass();
		this.name = o.getName();
		if (o instanceof ImmutableObjectInstance) {
			ImmutableObjectInstance immutable = (ImmutableObjectInstance)o;
			this.hashCode = immutable.hashCode;
			this.values = immutable.values;
			this.identifierIndependent = immutable.identifierIndependent; 
					
		} else {
			this.hashCode = 0;
			this.values = ImmutableList.copyOf(o.getValues());
			this.identifierIndependent = false;
		}
			
	}
	
	public ImmutableObjectInstance(ObjectClass obClass, String name, ImmutableList<Value> newValues, int hashCode, boolean identifierIndependent) {
		this.obClass = obClass;
		this.name = name;
		this.values = newValues;
		this.hashCode = hashCode;
		this.identifierIndependent = identifierIndependent;
	}
	
	
	public ImmutableObjectInstance(String name, ImmutableObjectInstance objectInstance) {
		this.obClass = objectInstance.obClass;
		this.name = name;
		this.values = objectInstance.values;
		this.hashCode = 0;
		this.identifierIndependent = objectInstance.identifierIndependent;
	}

	/**
	 * Creates and returns a new object instance that is a deep copy of this object instance's values.
	 * @return a new object instance that is a deep copy of this object instance's values (the name and object class reference are a shallow copy)
	 */
	public ImmutableObjectInstance copy(){
		return new ImmutableObjectInstance(this);
	}
	
	private static ImmutableObjectInstance construct(ObjectClass obClass, String name, List<Value> values, boolean identifierIndependent) {
		return new ImmutableObjectInstance(obClass, name, ImmutableList.copyOf(values), 0, identifierIndependent);
	}
	
	/**
	 * Creates new value object assignments for each of this object instance class's attributes.
	 */
	public List <Value> initializeValueObjects(){
		
		List <Value> values = new ArrayList <Value>(obClass.numAttributes());
		for(Attribute att : obClass.attributeList){
			values.add(att.valueConstructor());
		}
		return values;
	}
	
	/**
	 * Sets the name of this object instance.
	 * @param name the name for this object instance.
	 */
	public ImmutableObjectInstance setName(String name){
		return ImmutableObjectInstance.construct(this.obClass, name, this.values, this.identifierIndependent);
	}
		
	public ImmutableObjectInstance setHashCode(int code, boolean identifierIndependent) {
		return new ImmutableObjectInstance(this.obClass, name, this.values, code, identifierIndependent);
	}
	
	/**
	 * Sets the value of the attribute named attName for this object instance.
	 * @param attName the name of the attribute whose value is to be set.
	 * @param v the string rep value to which the attribute of this object instance should be set.
	 */
	public ImmutableObjectInstance setValue(String attName, String v){
		List<Value> values = new ArrayList<Value>(this.values);
		int ind = obClass.attributeIndex(attName);
		Value value = values.get(ind);
		values.set(ind, value.setValue(v));
		return ImmutableObjectInstance.construct(this.obClass, this.name, values, this.identifierIndependent);
		
	}
	
	/**
	 * Sets the value of the attribute named attName for this object instance.
	 * @param attName the name of the attribute whose value is to be set.
	 * @param v the double rep value to which the attribute of this object instance should be set.
	 */
	public ImmutableObjectInstance setValue(String attName, double v){
		List<Value> values = new ArrayList<Value>(this.values);
		int ind = obClass.attributeIndex(attName);
		Value value = values.get(ind);
		values.set(ind, value.setValue(v));
		return ImmutableObjectInstance.construct(this.obClass, this.name, values, this.identifierIndependent);
	}
	
	/**
	 * Sets the value of the attribute named attName for this object instance.
	 * @param attName the name of the attribute whose value is to be set.
	 * @param v the int rep value to which the attribute of this object instance should be set.
	 */
	public ImmutableObjectInstance setValue(String attName, int v){
		Value[] arry = this.values.toArray(new Value[this.values.size()]);
		int ind = obClass.attributeIndex(attName);
		arry[ind] = arry[ind].setValue(v);
		return ImmutableObjectInstance.construct(obClass, name, ImmutableList.copyOf(arry), this.identifierIndependent);
	}
	
	/**
	 * Sets the value of the attribute named attName for this object instance.
	 * @param attName the name of the attribute whose value is to be set.
	 * @param v the int rep value to which the attribute of this object instance should be set.
	 */
	public ImmutableObjectInstance setValue(String attName, boolean v){
		List<Value> values = new ArrayList<Value>(this.values);
		int ind = obClass.attributeIndex(attName);
		Value value = values.get(ind);
		values.set(ind, value.setValue(v));
		return ImmutableObjectInstance.construct(this.obClass, this.name, values, this.identifierIndependent);
	}
	
	/**
	 * Sets the value of the attribute named attName for this object instance.
	 * @param attName the name of the attribute whose value is to be set.
	 * @param v the int array rep value to which the attribute of this object instance should be set.
	 */
	public ImmutableObjectInstance setValue(String attName, int [] v){
		List<Value> values = new ArrayList<Value>(this.values);
		int ind = obClass.attributeIndex(attName);
		Value value = values.get(ind);
		values.set(ind, value.setValue(v));
		return ImmutableObjectInstance.construct(this.obClass, this.name, values, this.identifierIndependent);
	}
	
	/**
	 * Sets the value of the attribute named attName for this object instance.
	 * @param attName the name of the attribute whose value is to be set.
	 * @param v the double array rep value to which the attribute of this object instance should be set.
	 */
	public ImmutableObjectInstance setValue(String attName, double [] v){
		List<Value> values = new ArrayList<Value>(this.values);
		int ind = obClass.attributeIndex(attName);
		Value value = values.get(ind);
		values.set(ind, value.setValue(v));
		return ImmutableObjectInstance.construct(this.obClass, this.name, values, this.identifierIndependent);
	}
	
	
	/**
	 * Sets the relational value of the attribute named attName for this object instance. If the
	 * attribute is a multi-target relational attribute, then this value is added to the target list.
	 * @param attName the name of the relational attribute that will have a relational target added/set
	 * @param target the name of the object reference that is to be added as a target.
	 */
	public ImmutableObjectInstance addRelationalTarget(String attName, String target){
		List<Value> values = new ArrayList<Value>(this.values);
		int ind = obClass.attributeIndex(attName);
		Value value = values.get(ind);
		values.set(ind, value.addRelationalTarget(target));
		return ImmutableObjectInstance.construct(this.obClass, this.name, values, this.identifierIndependent);
	}
	
	/**
	 * Adds all relational targets to the attribute attName for this object instance. For a single-target
	 * relational attribute, the value that is ultimately set depends on the iteration order of iterable. 
	 * @param attName the name of the relational attribute that will have a relational target added/set
	 * @param targets the names of the object references that are to be added as a targets.
	 */
	public ImmutableObjectInstance addAllRelationalTargets(String attName, Collection<String> targets) {
		List<Value> values = new ArrayList<Value>(this.values);
		int ind = obClass.attributeIndex(attName);
		Value value = values.get(ind);
		values.set(ind, value.addAllRelationalTargets(targets));
		return ImmutableObjectInstance.construct(this.obClass, this.name, values, this.identifierIndependent);
	}
	
	/**
	 * Clears all the relational value targets of the attribute named attName for this object instance.
	 * @param attName
	 */
	public ImmutableObjectInstance clearRelationalTargets(String attName){
		List<Value> values = new ArrayList<Value>(this.values);
		int ind = obClass.attributeIndex(attName);
		Value value = values.get(ind);
		values.set(ind, value.clearRelationTargets());
		return ImmutableObjectInstance.construct(this.obClass, this.name, values, this.identifierIndependent);
	}
	
	/**
	 * Removes an object target from the specified relational attribute.
	 * @param attName the name of the relational attribute from which the target should be removed.
	 * @param target the target to remove from the relational attribute value.
	 */
	public ImmutableObjectInstance removeRelationalTarget(String attName, String target){
		List<Value> values = new ArrayList<Value>(this.values);
		int ind = obClass.attributeIndex(attName);
		Value value = values.get(ind);
		values.set(ind, value.removeRelationalTarget(target));
		return ImmutableObjectInstance.construct(this.obClass, this.name, values, this.identifierIndependent);
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
	public String getClassName(){
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
	public int getIntValForAttribute(String attName){
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
	public boolean getBooleanValForAttribute(String attName){
		int ind = obClass.attributeIndex(attName);
		return values.get(ind).getBooleanValue();
	}
	
	/**
	 * Returns the int array value of the attribute (only defined for int array attributes).
	 * @param attName the name of the attribute whose value should be returned.
	 * @return the int array value.
	 */
	public int [] getIntArrayValForAttribute(String attName){
		int ind = obClass.attributeIndex(attName);
		return values.get(ind).getIntArray().clone();
	}
	
	
	/**
	 * Returns the int array value of the attribute (only defined for int array attributes).
	 * @param attName the name of the attribute whose value should be returned.
	 * @return the int array value.
	 */
	public double [] getDoubleArrayValForAttribute(String attName){
		int ind = obClass.attributeIndex(attName);
		return values.get(ind).getDoubleArray().clone();
	}
	
	
	/**
	 * Returns the list of value object assignments to all of this object instance's attributes.
	 * @return the list of value object assignments to all of this object instance's attributes.
	 */
	public List <Value> getValues(){
		return this.values;
	}
	
	public StringBuilder buildObjectDescription(StringBuilder builder) {
		builder = builder.append(name).append(" (").append(this.getClassName()).append(")");
		for(Value v : values){
			builder = builder.append("\n\t").append(v.attName()).append(":\t");
			builder = v.buildStringVal(builder);
		}
		
		return builder;
	}
	
	@Override
	public String toString() {
		return this.getObjectDescription();
	}

	/**
	 * Returns a double vector of all the observable values in this object instance. Discrete values have
	 * their int stored valued converted to a double for this array. This method will throw a runtime exception
	 * if the object instance includes relational values. This method may be useful if objects need to be indexed
	 * in data structures like kd-trees.
	 * @return a double vector of all the observable values in this object instance.
	 */
	@Override
	public double[] getFeatureVec(){
		
		double [] obsFeatureVec = new double[obClass.numAttributes()];
		for(int i = 0; i < obsFeatureVec.length; i++){
			obsFeatureVec[i] = values.get(i).getNumericRepresentation();
		}
		
		return obsFeatureVec;
	}
	
	
	/**
	 * Returns a normalized double vector of all the observable values in this object instance. This method relies on the lowerlims and upperlims 
	 * being set for the corresponding attribute. Furthermore, this method will throw a runtime exception
	 * if the object instance includes attributes that are *not* type REAL or INT.
	 * @return a normalized double vector of all the observable values in this object instance.
	 */
	@Override
	public double [] getNormalizedFeatureVec(){
		
		double [] obsFeatureVec = new double[obClass.numAttributes()];
		for(int i = 0; i < obsFeatureVec.length; i++){
			Value v = values.get(i);
			Attribute a = v.getAttribute();
			if(a.type != AttributeType.REAL && a.type != AttributeType.INT){
				throw new RuntimeException("Cannot get a normalized numeric value for attribute " + a.name + " because it is not a REAL or INT type.");
			}
			double dv = values.get(i).getNumericRepresentation();
			double n = (dv - a.lowerLim) / (a.upperLim - a.lowerLim);
			obsFeatureVec[i] = n;
		}
		
		return obsFeatureVec;
		
	}
	
	
	@Override
	public boolean equals(Object obj){
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ImmutableObjectInstance)) {
			return false;
		}
		ImmutableObjectInstance op = (ImmutableObjectInstance)obj;
		if (!this.identifierIndependent && !op.name.equals(name)) {
		    return false; 
		}
		return this.valueEquals(op);
	}
	
	
	/**
	 * Returns true if the value assignments in this object instance are the same as they are in the target object instance.
	 * @param obj the object instance against which this object instance should be compared.
	 * @return true if this object instance and obj have identical value assignments; false otherwise.
	 */
	public boolean valueEquals(ObjectInstance obj){
		if (this == obj) {
			return true;
		}
		
		if (!obj.getObjectClass().equals(this.obClass)) {
			return false;
		}
	
		List<Value> objValues = obj.getValues();
 		for (int i = 0; i < this.values.size(); i++) {
 			Value v = this.values.get(i);
 			Value ov = objValues.get(i);
 			if (!v.equals(ov)) {
 				return false;
			}
		}
		
		return true;
	
	}
	
	public boolean isHashed() {
		return this.hashCode != 0;
	}
	
	@Override
	public int hashCode() {
		if (this.hashCode == 0) {
			return super.hashCode();
		}
		return this.hashCode;
	}
	
	@Override
	public List<String> unsetAttributes() {
		LinkedList<String> unsetAtts = new LinkedList<String>();
		for(Value v : this.values){
			if(!v.valueHasBeenSet()){
				unsetAtts.add(v.attName());
			}
		}
		return unsetAtts;
	}

	@Override
	public String getObjectDescriptionWithNullForUnsetAttributes() {
		StringBuilder builder = new StringBuilder();
		builder = builder.append(name).append(" (").append(this.getClassName()).append(")\n");
		for(Value v : values){
			if(v.valueHasBeenSet()) {
				builder = builder.append("\t").append(v.attName()).append(":\t");
				builder = v.buildStringVal(builder).append("\n");
			}
			else{
				builder = builder.append("\t").append(v.attName()).append(":\tnull\n");
			}
		}

		return builder.toString();
	}

	@Override
	public Iterator<Value> iterator() {
		return this.values.iterator();
	}
}
