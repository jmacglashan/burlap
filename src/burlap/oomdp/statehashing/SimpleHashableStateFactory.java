package burlap.oomdp.statehashing;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.core.values.Value;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A straightforward factory for creating {@link burlap.oomdp.statehashing.HashableState} objects from
 * {@link burlap.oomdp.core.states.State} instances. By default, this factory will be object identifier independent
 * (the names of objects don't affect the state definition). However, you can make it object identifier dependent
 * with the either of the constructors {@link #SimpleHashableStateFactory(boolean)} or
 * {@link #SimpleHashableStateFactory(boolean, boolean)}.
 * <br/><br/>
 * This factory is capable of hashing states with any kind of values. However, if you wish to hash states
 * that have relational attributes, you must set the factory to be object identifier dependent.
 * <br/><br/>
 * Optionally, this factory can be set to produce {@link burlap.oomdp.statehashing.HashableState} instances
 * that cache the hash code so that it does not need to be recomputed on multiple calls of the hashCode method.
 * To enable hash code caching, use the {@link #SimpleHashableStateFactory(boolean, boolean)} constructor.
 * Using caching will use slightly more memory by having to associate an int with each {@link burlap.oomdp.statehashing.HashableState}.
 * <br/><br/>
 * This class has multiple aspects of the state equality methods implemented so that it can be easily sub classed
 * by other forms of equality checking and have each method override only what it needs to override.
 * @author James MacGlashan.
 */
public class SimpleHashableStateFactory implements HashableStateFactory {

	/**
	 * Whether state evaluations are object identifier independent (the names of objects don't matter). By
	 * default it is independent.
	 */
	protected boolean identifierIndependent = true;

	/**
	 * Whether to cache the hash code for each produced {@link burlap.oomdp.statehashing.HashableState}.
	 * Default is non-cached.
	 */
	protected boolean useCached = false;

	/**
	 * Classes of {@link burlap.oomdp.core.Attribute.AttributeType} that affect how hashing will be performed
	 */
	protected static enum AttClass {INT, DOUBLE, INTARRAY, DOUBLEARRAY, STRING, RELATIONAL}


	/**
	 * Default constructor: object identifier independent and no hash code caching.
	 */
	public SimpleHashableStateFactory(){

	}

	/**
	 * Initializes with no hash code caching.
	 * @param identifierIndependent if true then state evaluations are object identifier independent; if false then dependent.
	 */
	public SimpleHashableStateFactory(boolean identifierIndependent){
		this.identifierIndependent = identifierIndependent;
	}

	/**
	 * Initializes.
	 * @param identifierIndependent if true then state evaluations are object identifier independent; if false then dependent.
	 * @param useCached if true then the hash code for each produced {@link burlap.oomdp.statehashing.HashableState} will be cached; if false then they will not be cached.
	 */
	public SimpleHashableStateFactory(boolean identifierIndependent, boolean useCached) {
		this.identifierIndependent = identifierIndependent;
		this.useCached = useCached;
	}

	@Override
	public HashableState hashState(State s) {
		if(s instanceof HashableState){
			if(s instanceof SimpleHashableStateInterface){
				if(((SimpleHashableStateInterface)s).getParentHashingFactory() == this){
					return (HashableState)s; //asking to hash what we've already hashed so just return it
				}
			}
		}

		if(useCached){
			return new SimpleCachedHashableState(s);
		}
		return new SimpleHashableState(s);
	}

	@Override
	public boolean objectIdentifierIndependent() {
		return this.identifierIndependent;
	}

	/**
	 * Computes the hash code for the input state.
	 * @param s the input state for which a hash code is to be computed
	 * @return the hash code
	 */
	protected int computeHashCode(State s){

		List<ObjectInstance> objects = s.getAllObjects();
		int [] hashCodes = new int[objects.size()];
		for(int i = 0; i < hashCodes.length; i++){
			hashCodes[i] = computeHashCode(objects.get(i));
		}
		//sort for invariance to order
		Arrays.sort(hashCodes);
		HashCodeBuilder hashCodeBuilder = new HashCodeBuilder(17, 31);
		for(int code : hashCodes){
			hashCodeBuilder.append(code);
		}
		int code = hashCodeBuilder.toHashCode();

		return code;
	}


	/**
	 * Computes the hash code for an individual {@link burlap.oomdp.core.objects.ObjectInstance}.
	 * @param o the {@link burlap.oomdp.core.objects.ObjectInstance} whose hash code will be computed.
	 * @return the hash code for the {@link burlap.oomdp.core.objects.ObjectInstance}.
	 */
	protected int computeHashCode(ObjectInstance o){

		HashCodeBuilder hashCodeBuilder = new HashCodeBuilder(17, 31);
		if(!this.identifierIndependent){
			hashCodeBuilder.append(o.getName());
		}

		List<Value> values = o.getValues();
		for(Value v : values){
			this.appendHashcodeForValue(hashCodeBuilder, v);
		}


		return hashCodeBuilder.toHashCode();
	}

	/**
	 * Appends the hash code for the given {@link burlap.oomdp.core.values.Value} to the {@link org.apache.commons.lang3.builder.HashCodeBuilder}
	 * @param hashCodeBuilder the {@link org.apache.commons.lang3.builder.HashCodeBuilder} to which the value's hash code will be appended
	 * @param v the {@link burlap.oomdp.core.values.Value} whose hash code should be appended.
	 */
	protected void appendHashcodeForValue(HashCodeBuilder hashCodeBuilder, Value v){
		AttClass attClass = getAttClass(v.getAttribute());
		if(attClass == AttClass.INT){
			hashCodeBuilder.append(v.getDiscVal());
		}
		else if(attClass == AttClass.DOUBLE){
			hashCodeBuilder.append(v.getNumericRepresentation());
		}
		else if(attClass == AttClass.INTARRAY){
			hashCodeBuilder.append(v.getIntArray());
		}
		else if(attClass == AttClass.DOUBLEARRAY){
			hashCodeBuilder.append(v.getDoubleArray());
		}
		else if(attClass == AttClass.STRING){
			hashCodeBuilder.append(v.getStringVal());
		}
		else if(attClass == AttClass.RELATIONAL){
			if(identifierIndependent){
				throw new RuntimeException("SimpleHashableStateFactory is set to be identifier independent, but attribute " + v.attName() + " is " +
						"relational which require identifier dependence. Instead, set SimpleHashableStateFactory to be idenitifer dependent.");
			}
			Set<String> targets = v.getAllRelationalTargets();
			for(String t : targets){
				hashCodeBuilder.append(t);
			}
		}
	}


	protected AttClass getAttClass(Attribute att){
		if(att.type == Attribute.AttributeType.INT || att.type == Attribute.AttributeType.DISC || att.type == Attribute.AttributeType.BOOLEAN){
			return AttClass.INT;
		}
		else if(att.type == Attribute.AttributeType.REAL || att.type == Attribute.AttributeType.REALUNBOUND){
			return AttClass.DOUBLE;
		}
		else if(att.type == Attribute.AttributeType.STRING){
			return AttClass.STRING;
		}
		else if(att.type == Attribute.AttributeType.INTARRAY){
			return AttClass.INTARRAY;
		}
		else if(att.type == Attribute.AttributeType.DOUBLEARRAY){
			return AttClass.DOUBLEARRAY;
		}
		else if(att.type == Attribute.AttributeType.RELATIONAL || att.type == Attribute.AttributeType.MULTITARGETRELATIONAL){
			return AttClass.RELATIONAL;
		}
		throw new RuntimeException("SimpleHashableStateFactory cannot hash value for attribute of type " + att.type);
	}


	/**
	 * Returns true if the two input states are equal. Equality respect this hashing factory's identifier independence
	 * setting.
	 * @param s1 a {@link burlap.oomdp.core.states.State}
	 * @param s2 another {@link burlap.oomdp.core.states.State} with which to compare
	 * @return true if s1 equals s2, false otherwise.
	 */
	protected boolean statesEqual(State s1, State s2) {
		if(this.identifierIndependent){
			return identifierIndependentEquals(s1, s2);
		}
		else{
			return identifierDependentEquals(s1, s2);
		}
	}


	/**
	 * Evaluates whether two states are equal when equality is independent of object identifiers/names being equal
	 * @param s1 the first {@link State} to compare
	 * @param s2 the second {@link State} to compare
	 * @return true if s1 = s2; false otherwise
	 */
	protected boolean identifierIndependentEquals(State s1, State s2){

		if(s1.numTotalObjects() != s2.numTotalObjects()){
			return false;
		}

		Set<String> matchedObjects = new HashSet<String>();
		for(List<ObjectInstance> objects : s1.getAllObjectsByClass()){

			String oclass = objects.get(0).getClassName();
			List <ObjectInstance> oobjects = s2.getObjectsOfClass(oclass);
			if(objects.size() != oobjects.size()){
				return false;
			}

			for(ObjectInstance o : objects){
				boolean foundMatch = false;
				for(ObjectInstance oo : oobjects){
					String ooname = oo.getName();
					if(matchedObjects.contains(ooname)){
						continue;
					}
					if(objectValuesEqual(o, oo)){
						foundMatch = true;
						matchedObjects.add(ooname);
						break;
					}
				}
				if(!foundMatch){
					return false;
				}
			}

		}


		return true;

	}

	/**
	 * Evaluates whether two states are equal when equality depends on object identifiers/names being equal.
	 * @param s1 the first {@link State} to compare
	 * @param s2 the second {@link State} to compare
	 * @return true if s1 = s2; false otherwise
	 */
	protected boolean identifierDependentEquals(State s1, State s2){

		if(s1.numTotalObjects() != s2.numTotalObjects()){
			return false;
		}

		List<ObjectInstance> theseObjects = s1.getAllObjects();
		if(theseObjects.size() != s2.numTotalObjects()){
			return false;
		}
		for(ObjectInstance ob : theseObjects){
			ObjectInstance oByName = s2.getObject(ob.getName());
			if(oByName == null){
				return false;
			}
			if(!objectValuesEqual(ob, oByName)){
				return false;
			}
		}

		return true;

	}


	/**
	 * Evaluates whether the values of two {@link burlap.oomdp.core.objects.ObjectInstance}s are equal.
	 * @param o1 the first {@link burlap.oomdp.core.objects.ObjectInstance} to compare
	 * @param o2 the second {@link burlap.oomdp.core.objects.ObjectInstance} to compare
	 * @return true if the values of o1 = o2; false otherwise.
	 */
	protected boolean objectValuesEqual(ObjectInstance o1, ObjectInstance o2){
		for(Value v : o1.getValues()){
			Value ov = o2.getValueForAttribute(v.attName());
			if(!valuesEqual(v, ov)){
				return false;
			}
		}
		return true;
	}


	/**
	 * Returns whether two values are equal.
	 * @param v1 the first value to compare
	 * @param v2 the second value to compare
	 * @return true if v1 = v2; false otherwise
	 */
	protected boolean valuesEqual(Value v1, Value v2){
		return v1.equals(v2);
	}


	/**
	 * An interface for {@link burlap.oomdp.statehashing.HashableState} instances that are created
	 * by the {@link burlap.oomdp.statehashing.SimpleHashableStateFactory}. It provides a method
	 * for checking that the parent factory is the same and is used for both cached and non-cached
	 * hash code {@link burlap.oomdp.statehashing.HashableState} instances.
	 */
	public static interface SimpleHashableStateInterface{
		HashableStateFactory getParentHashingFactory();
	}

	protected class SimpleCachedHashableState extends HashableState.CachedHashableState implements SimpleHashableStateInterface{

		public SimpleCachedHashableState(State s) {
			super(s);
		}

		@Override
		public int computeHashCode() {
			return SimpleHashableStateFactory.this.computeHashCode(this.s);
		}

		@Override
		public boolean equals(Object other) {
			if(this == other){
				return true;
			}
			if(!(other instanceof HashableState)){
				return false;
			}
			HashableState o = (HashableState)other;
			return statesEqual(this.s, o.s);
		}

		@Override
		public State copy() {
			return new SimpleCachedHashableState(this.s.copy());
		}

		@Override
		public HashableStateFactory getParentHashingFactory(){
			return SimpleHashableStateFactory.this;
		}

	}

	protected class SimpleHashableState extends HashableState implements SimpleHashableStateInterface{

		public SimpleHashableState(State s) {
			super(s);
		}

		@Override
		public int hashCode() {
			return SimpleHashableStateFactory.this.computeHashCode(this.s);
		}

		@Override
		public boolean equals(Object other) {
			if(this == other){
				return true;
			}
			if(!(other instanceof HashableState)){
				return false;
			}
			HashableState o = (HashableState)other;
			return statesEqual(this.s, o.s);
		}

		@Override
		public State copy() {
			return new SimpleHashableState(this.s.copy());
		}

		@Override
		public HashableStateFactory getParentHashingFactory(){
			return SimpleHashableStateFactory.this;
		}
	}

}
