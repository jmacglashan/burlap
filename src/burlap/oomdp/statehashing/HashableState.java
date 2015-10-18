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
 * access the state it hashes from the public data member {@link #s}. If the {@link burlap.oomdp.core.states.State}
 * delegate {@link #s} is a {@link burlap.oomdp.statehashing.HashableState} itself, and you wish
 * to get the underlying {@link burlap.oomdp.core.states.State}, then you should use the
 * {@link #getSourceState()} method, which will recursively descend and return the base source {@link burlap.oomdp.core.states.State}.
 * <br/><br/>
 * Implementing this class requires implementing
 * the {@link #hashCode()} and {@link #equals(Object)} method.
 * <br/><br/>
 * Note that this class implements the {@link burlap.oomdp.core.states.State} interface; however,
 * because the purpose of this class is to used with hashed data structures, it is not recommended that
 * you modify the state.
 *
 * @author James MacGlashan
 *
 */
public abstract class HashableState implements State{

	/**
	 * The source {@link burlap.oomdp.core.states.State} to be hashed and evaluated by the {@link #hashCode()} and {@link #equals(Object)} method.
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
	public abstract boolean equals(Object obj);


	@Override
	public State addObject(ObjectInstance o) {
		return s.addObject(o);
	}

	@Override
	public State addAllObjects(Collection<ObjectInstance> objects) {
		return s.addAllObjects(objects);
	}

	@Override
	public State removeObject(String oname) {
		return s.removeObject(oname);
	}

	@Override
	public State removeObject(ObjectInstance o) {
		return s.removeObject(o);
	}

	@Override
	public State removeAllObjects(Collection<ObjectInstance> objects) {
		return s.removeAllObjects(objects);
	}

	@Override
	public State renameObject(String originalName, String newName) {
		return s.renameObject(originalName, newName);
	}

	@Override
	public State renameObject(ObjectInstance o, String newName) {
		return s.renameObject(o, newName);
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
	
	public <T> State setObjectsValue(String objectName, String attName, T value) {
		return s.setObjectsValue(objectName, attName, value);
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
		public int hashCode(){
			if(needToRecomputeHashCode){
				this.hashCode = this.computeHashCode();
				this.needToRecomputeHashCode = false;
			}
			return hashCode;
		}

	}


}



