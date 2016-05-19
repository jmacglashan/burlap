package burlap.statehashing.simple;

import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.statehashing.HashableState;
import burlap.statehashing.WrappedHashableState;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.*;

/**
 * @author James MacGlashan.
 */
public class IISimpleHashableState extends WrappedHashableState {

	public IISimpleHashableState() {
	}

	public IISimpleHashableState(State s) {
		super(s);
	}

	@Override
	public int hashCode() {
		return computeHashCode(this.s);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this){
			return true;
		}
		if(!(obj instanceof HashableState)){
			return false;
		}
		return statesEqual(this.s, ((HashableState)obj).s());
	}

	@Override
	public IISimpleHashableState copy() {
		return new IISimpleHashableState(s.copy());
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
			int totalHash = oHash + 31*classNameHash;
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



}
