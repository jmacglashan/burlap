package burlap.oomdp.statehashing;


import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * This class provides a hash value for {@link burlap.oomdp.core.states.State} objects. This is useful for tabular
 * planning and learning algorithms that make use of hash-backed sets or maps for fast retrieval. You can
 * access the state it hashes from the public data member s.
 * <p/>
 * By default, equality checks use the standard {@link burlap.oomdp.core.states.State} object equality check. If you need
 * to handle this specially, (such as providing state abstraction), then the equals method should be overridden.
 * <br/><br/>
 * Note that this class implements the {@link burlap.oomdp.core.states.State} interface; however,
 * because the purpose of this class is to used with hashed data structures, it is not recommended that
 * you modify the state and for that reason the state modifying methods will throw runtime exceptions. If you
 * wish to edit state anyway, get the underlying source state, either by directly accessing the public data member
 * {@link #s} or by using the {@link #getSourceState()} method, which will recurisively return the underlying
 * {@link burlap.oomdp.core.states.State} even if {@link #s} is a {@link burlap.oomdp.statehashing.HashableState} itself.
 *
 * @author James MacGlashan
 *
 */
public abstract class HashableState implements State{

	/**
	 * The state to be hashed
	 */
	public State s;

	public HashableState(State s){
		this.s = s;
	}

	/**
	 * Returns the underlying source state is hashed. If the delegate {@link burlap.oomdp.core.states.State}
	 * of this {@link burlap.oomdp.statehashing.HashableState} is also a {@link burlap.oomdp.statehashing.HashableState},
	 * then it recursively returns its {@link #getSourceState()}.
	 * @return The underlying source {@link burlap.oomdp.core.states.State} that this object hashes and evaluates.
	 */
	public State getSourceState(){
		if(this.s instanceof HashableState){
			return ((HashableState)this.s).getSourceState();
		}
		return this.s;
	}

	@Override
	public abstract int hashCode();

	@Override
	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if(!(obj instanceof HashableState)){
			return false;
		}
		HashableState o = (HashableState)obj;
		return s.equals(o.s);
	}


	@Override
	public State addObject(ObjectInstance o) {
		throw new RuntimeException("HashableState instances do not support state modifying methods since modifying a hashed state can corrupt its index in Hashed data structures. If you wish to edit the state anyway, use the getSourceStateMethod and edit the underlying state directly.");
	}

	@Override
	public State addAllObjects(Collection<ObjectInstance> objects) {
		throw new RuntimeException("HashableState instances do not support state modifying methods since modifying a hashed state can corrupt its index in Hashed data structures. If you wish to edit the state anyway, use the getSourceStateMethod and edit the underlying state directly.");
	}

	@Override
	public State removeObject(String oname) {
		throw new RuntimeException("HashableState instances do not support state modifying methods since modifying a hashed state can corrupt its index in Hashed data structures. If you wish to edit the state anyway, use the getSourceStateMethod and edit the underlying state directly.");
	}

	@Override
	public State removeObject(ObjectInstance o) {
		throw new RuntimeException("HashableState instances do not support state modifying methods since modifying a hashed state can corrupt its index in Hashed data structures. If you wish to edit the state anyway, use the getSourceStateMethod and edit the underlying state directly.");
	}

	@Override
	public State removeAllObjects(Collection<ObjectInstance> objects) {
		throw new RuntimeException("HashableState instances do not support state modifying methods since modifying a hashed state can corrupt its index in Hashed data structures. If you wish to edit the state anyway, use the getSourceStateMethod and edit the underlying state directly.");
	}

	@Override
	public State renameObject(String originalName, String newName) {
		throw new RuntimeException("HashableState instances do not support state modifying methods since modifying a hashed state can corrupt its index in Hashed data structures. If you wish to edit the state anyway, use the getSourceStateMethod and edit the underlying state directly.");
	}

	@Override
	public State renameObject(ObjectInstance o, String newName) {
		throw new RuntimeException("HashableState instances do not support state modifying methods since modifying a hashed state can corrupt its index in Hashed data structures. If you wish to edit the state anyway, use the getSourceStateMethod and edit the underlying state directly.");
	}

	@Override
	public Map<String, String> getObjectMatchingTo(State so, boolean enforceStateExactness) {
		return s.getObjectMatchingTo(so, enforceStateExactness);
	}

	@Override
	public int numTotalObjects() {
		return s.numTotalObjects();
	}

	@Override
	public ObjectInstance getObject(String oname) {
		return s.getObject(oname);
	}

	@Override
	public List<ObjectInstance> getAllObjects() {
		return s.getAllObjects();
	}

	@Override
	public List<ObjectInstance> getObjectsOfClass(String oclass) {
		return s.getObjectsOfClass(oclass);
	}

	@Override
	public ObjectInstance getFirstObjectOfClass(String oclass) {
		return s.getFirstObjectOfClass(oclass);
	}

	@Override
	public Set<String> getObjectClassesPresent() {
		return s.getObjectClassesPresent();
	}

	@Override
	public List<List<ObjectInstance>> getAllObjectsByClass() {
		return s.getAllObjectsByClass();
	}

	@Override
	public String getStateDescription() {
		return s.getStateDescription();
	}

	@Override
	public String getCompleteStateDescription() {
		return s.getCompleteStateDescription();
	}

	@Override
	public Map<String, List<String>> getAllUnsetAttributes() {
		return s.getAllUnsetAttributes();
	}

	@Override
	public String getCompleteStateDescriptionWithUnsetAttributesAsNull() {
		return s.getCompleteStateDescriptionWithUnsetAttributesAsNull();
	}

	@Override
	public List<List<String>> getPossibleBindingsGivenParamOrderGroups(String[] paramClasses, String[] paramOrderGroups) {
		return s.getPossibleBindingsGivenParamOrderGroups(paramClasses, paramOrderGroups);
	}

	/**
	 * A hash code cached abstract implementation of {@link HashableState}.
	 * Once a hash code is computed, it is saved so that it does not need to be used again. Implementing
	 * this class only requires implementing {@link #computeHashCode()}.
	 */
	public static abstract class CachedHashableState extends HashableState {

		protected int								hashCode;
		protected boolean							needToRecomputeHashCode;



		/**
		 * Initializes the StateHashTuple with the given {@link burlap.oomdp.core.states.State} object.
		 * @param s the state object this object will wrap
		 */
		public CachedHashableState(State s){
			super(s);
			needToRecomputeHashCode = true;
		}


		/**
		 * This method computes the hashCode for this {@link HashableState}
		 * @return the hashcode for this state
		 */
		public abstract int computeHashCode();


		@Override
		public boolean equals(Object other){
			if(this == other){
				return true;
			}
			if(!(other instanceof HashableState)){
				return false;
			}
			HashableState o = (HashableState)other;
			return s.equals(o.s);

		}

		@Override
		public int hashCode(){
			if(needToRecomputeHashCode){
				this.hashCode = this.computeHashCode();
				this.needToRecomputeHashCode = false;
			}
			return hashCode;
		}

	}


}



