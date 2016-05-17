package burlap.statehashing;

import burlap.mdp.core.state.State;
import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.*;

/**
 * A straightforward factory for creating {@link burlap.statehashing.HashableState} objects from
 * {@link State} instances. The general approach is that hash values are computed by iterating through each
 * variable key in the order returned by {@link State#variableKeys()} and the has code for values returned by
 * {@link State#get(Object)} are combined. Similarly, two states are evaluated as equal when the values returned by
 * {@link State#get(Object)} satisfy their implemented {@link Object#equals(Object)} method.
 * <p>
 * This class also automatically provides special treatment for OO-MDP states (states that implement
 * {@link burlap.mdp.core.oo.state.OOState}) by being object identifier independent
 * (the names of objects don't affect the state identity). However, you may disable identifier independence
 * by using the constructor {@link #SimpleHashableStateFactory(boolean)}. If your domain is relational, it may be
 * important to be identifier *dependent* (that is, set the parameter in the constructor to false).
 * <p>
 * Optionally, this factory can be set to produce {@link burlap.statehashing.HashableState} instances
 * that cache the hash code so that it does not need to be recomputed on multiple calls of the hashCode method.
 * To enable hash code caching, use the {@link #SimpleHashableStateFactory(boolean, boolean)} constructor.
 * Using caching will use slightly more memory by having to associate an int with each {@link burlap.statehashing.HashableState}.
 * <p>
 * This class has multiple aspects of the state equality methods implemented so that it can be easily sub classed
 * by other forms of equality checking and have each method override only what it needs to override.
 * @author James MacGlashan.
 */
public class SimpleHashableStateFactory implements HashableStateFactory.OOHashableStateFactory {

	/**
	 * Whether state evaluations of OO-MDPs are object identifier independent (the names of objects don't matter). By
	 * default it is independent.
	 */
	protected boolean identifierIndependent = true;

	/**
	 * Whether to cache the hash code for each produced {@link burlap.statehashing.HashableState}.
	 * Default is non-cached.
	 */
	protected boolean useCached = false;



	/**
	 * Default constructor: object identifier independent and no hash code caching.
	 */
	public SimpleHashableStateFactory(){

	}

	/**
	 * Initializes with no hash code caching.
	 * @param identifierIndependent if true then state evaluations for {@link burlap.mdp.core.oo.state.OOState}s are object identifier independent; if false then dependent.
	 */
	public SimpleHashableStateFactory(boolean identifierIndependent){
		this.identifierIndependent = identifierIndependent;
	}

	/**
	 * Initializes.
	 * @param identifierIndependent if true then state evaluations for {@link burlap.mdp.core.oo.state.OOState}s  are object identifier independent; if false then dependent.
	 * @param useCached if true then the hash code for each produced {@link burlap.statehashing.HashableState} will be cached; if false then they will not be cached.
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
	protected final int computeHashCode(State s){

		if(s instanceof OOState){
			return computeOOHashCode((OOState)s);
		}
		return computeFlatHashCode(s);

	}

	protected int computeOOHashCode(OOState s){

		int [] hashCodes = new int[s.numObjects()];
		List<ObjectInstance> objects = s.objects();
		for(int i = 0; i < hashCodes.length; i++){
			ObjectInstance o = objects.get(i);
			int oHash = this.computeFlatHashCode(o);
			int classNameHash = o.className().hashCode();
			int nameHash = this.objectIdentifierIndependent() ? 0 : o.name().hashCode();
			int totalHash = oHash + 31*classNameHash + 31*31*nameHash;
			hashCodes[i] = totalHash;
		}

		//sort for invariance to order
		Arrays.sort(hashCodes);
		HashCodeBuilder hashCodeBuilder = new HashCodeBuilder(17, 31);
		hashCodeBuilder.append(hashCodes);
		return hashCodeBuilder.toHashCode();

	}

	protected int computeFlatHashCode(State s){

		HashCodeBuilder hashCodeBuilder = new HashCodeBuilder(17, 31);

		List<Object> keys = s.variableKeys();
		for(Object key : keys){
			Object value = s.get(key);
			this.appendHashCodeForValue(hashCodeBuilder, key, value);
		}

		return hashCodeBuilder.toHashCode();
	}

	protected void appendHashCodeForValue(HashCodeBuilder hashCodeBuilder, Object key, Object value){
		hashCodeBuilder.append(value);
	}


	/**
	 * Returns true if the two input states are equal. Equality respect this hashing factory's identifier independence
	 * setting.
	 * @param s1 a {@link State}
	 * @param s2 another {@link State} with which to compare
	 * @return true if s1 equals s2, false otherwise.
	 */
	protected boolean statesEqual(State s1, State s2) {

		if(s1 instanceof OOState && s2 instanceof OOState){
			return ooStatesEqual((OOState)s1, (OOState)s2);
		}
		return flatStatesEqual(s1, s2);


	}


	protected boolean ooStatesEqual(OOState s1, OOState s2){
		if(this.identifierIndependent){
			return identifierIndependentEquals(s1, s2);
		}
		else{
			return identifierDependentEquals(s1, s2);
		}
	}

	protected boolean flatStatesEqual(State s1, State s2){

		if(s1 == s2){
			return true;
		}

		List<Object> keys1 = s1.variableKeys();
		List<Object> keys2 = s2.variableKeys();

		if(keys1.size() != keys2.size()){
			return false;
		}

		for(Object key : keys1){
			Object v1 = s1.get(key);
			Object v2 = s2.get(key);
			if(!this.valuesEqual(key, v1, v2)){
				return false;
			}
		}
		return true;

	}



	/**
	 * Evaluates whether two {@link OOState}s are equal when equality is independent of object identifiers/names being equal
	 * @param s1 the first {@link OOState} to compare
	 * @param s2 the second {@link OOState} to compare
	 * @return true if s1 = s2; false otherwise
	 */
	protected boolean identifierIndependentEquals(OOState s1, OOState s2){
		if(s1 == s2){
			return true;
		}
		if(s1.numObjects() != s2.numObjects()){
			return false;
		}

		Set<String> matchedObjects = new HashSet<String>();
		for(Map.Entry<String, List<ObjectInstance>> e1 : OOStateUtilities.objectsByClass(s1).entrySet()){
			String oclass = e1.getKey();
			List<ObjectInstance> objects = e1.getValue();

			List<ObjectInstance> oobjects = s2.objectsOfClass(oclass);
			if(objects.size() != oobjects.size()){
				return false;
			}

			for(ObjectInstance o : objects){
				boolean foundMatch = false;
				for(ObjectInstance oo : oobjects){
					String ooname = oo.name();
					if(matchedObjects.contains(ooname)){
						continue;
					}
					if(flatStatesEqual(o, oo)){
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
	protected boolean identifierDependentEquals(OOState s1, OOState s2){

		if (s1 == s2) {
			return true;
		}
		if(s1.numObjects() != s2.numObjects()){
			return false;
		}

		List<ObjectInstance> theseObjects = s1.objects();
		for(ObjectInstance ob : theseObjects){
			ObjectInstance oByName = s2.object(ob.name());
			if(oByName == null){
				return false;
			}
			if(!flatStatesEqual(ob, oByName)){
				return false;
			}
		}

		return true;

	}

	/**
	 * Returns whether two values are equal.
	 * @param key the state variable key
	 * @param v1 the first value to compare
	 * @param v2 the second value to compare
	 * @return true if v1 = v2; false otherwise
	 */
	protected boolean valuesEqual(Object key, Object v1, Object v2){
		if(v1.getClass().isArray() && v2.getClass().isArray()){
			return Arrays.equals((Object[])v1, (Object[])v2);
		}
		return v1.equals(v2);
	}



	/**
	 * An interface for {@link burlap.statehashing.HashableState} instances that are created
	 * by the {@link burlap.statehashing.SimpleHashableStateFactory}. It provides a method
	 * for checking that the parent factory is the same and is used for both cached and non-cached
	 * hash code {@link burlap.statehashing.HashableState} instances.
	 */
	public interface SimpleHashableStateInterface{
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
