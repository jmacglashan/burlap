package burlap.oomdp.statehashing;

import burlap.oomdp.core.state.State;
import burlap.oomdp.core.oo.state.OOState;
import burlap.oomdp.core.oo.state.OOStateUtilities;
import burlap.oomdp.core.oo.state.ObjectInstance;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.*;

/**
 * This class produces {@link burlap.oomdp.statehashing.HashableState} instances in which the hash code and equality
 * of the states masks (ignores) specified state variables. For {@link burlap.oomdp.core.oo.state.OOState}s,
 * this class can also be specified to mask entire OO-MDP objects belonging to specified OO-MDP classes.
 * <p>
 * If masks are specified for variables, and the state is an {@link burlap.oomdp.core.oo.state.OOState} then the
 * variables names specified for the masks are assumed to be on the object-level. Therefore, if two different objects
 * have the same set of variables keys, a single mask for the variable name key will mask variable values for all objects
 * that have that key.
 * <p>
 * This class extends {@link burlap.oomdp.statehashing.SimpleHashableStateFactory}, which means it can be toggled to
 * to be object identifier/name independent or dependent and can be set to use {@link burlap.oomdp.statehashing.HashableState}
 * instances that cache their hash code or not. See the {@link burlap.oomdp.statehashing.SimpleHashableStateFactory}
 * class documentation for more information on those features.
 *
 * @author James MacGlashan.
 */
public class MaskedHashableStateFactory extends SimpleHashableStateFactory {

	protected Set<Object> maskedVariables = new HashSet<Object>();
	protected Set<String> maskedObjectClasses = new HashSet<String>();


	/**
	 * Default constructor: object identifier independent, no hash code caching, and no object class or attribute masks.
	 */
	public MaskedHashableStateFactory() {
	}


	/**
	 * Initializes with no hash code caching and no object class or attribute masks.
	 * @param identifierIndependent if true then state evaluations are object identifier independent; if false then dependent.
	 */
	public MaskedHashableStateFactory(boolean identifierIndependent) {
		super(identifierIndependent);
	}


	/**
	 * Initializes with no object class or variable masks.
	 * @param identifierIndependent if true then state evaluations are object identifier independent; if false then dependent.
	 * @param useCached if true then the hash code for each produced {@link burlap.oomdp.statehashing.HashableState} will be cached; if false then they will not be cached.
	 */
	public MaskedHashableStateFactory(boolean identifierIndependent, boolean useCached) {
		super(identifierIndependent, useCached);
	}


	/**
	 * Initializes with a specified variable or object class mask.
	 * @param identifierIndependent if true then state evaluations are object identifier independent; if false then dependent.
	 * @param useCached if true then the hash code for each produced {@link burlap.oomdp.statehashing.HashableState} will be cached; if false then they will not be cached.
	 * @param maskNamesAreForVariables whether the specified masks are masks for state variables or object classes. True for variables, false for object classes.
	 * @param masks the names of the state variables or OO-MDP object class that will be masked (ignored from state hashing and equality checks)
	 */
	public MaskedHashableStateFactory(boolean identifierIndependent, boolean useCached, boolean maskNamesAreForVariables, String... masks) {
		super(identifierIndependent, useCached);
		if(maskNamesAreForVariables){
			for(String mask : masks){
				this.maskedVariables.add(mask);
			}
		}
		else{
			for(String mask : masks){
				this.maskedObjectClasses.add(mask);
			}
		}
	}

	/**
	 * Adds masks for specific state variables. Mask keys should match what is returned by the {@link State#variableKeys()} method.
	 * @param masks keys of the state variables to mask
	 */
	public void addVariableMasks(Object...masks){
		for(Object mask : masks){
			this.maskedVariables.add(mask);
		}
	}

	/**
	 * Adds masks for entire OO-MDP objects that belong to the specified OO-MDP object class.
	 * @param masks the names of the object classes to mask.
	 */
	public void addObjectClassMasks(String...masks){
		for(String mask : masks){
			this.maskedObjectClasses.add(mask);
		}
	}


	/**
	 * Removes variable masks.
	 * @param masks variable keys for which masks should be removed
	 */
	public void removeAttributeMasks(Object...masks){
		for(Object mask : masks){
			this.maskedVariables.remove(mask);
		}
	}


	/**
	 * Removes masks for OO-MDP object classes
	 * @param masks the names object classes that will no longer be masked.
	 */
	public void removeObjectClassMasks(String...masks){
		for(String mask : masks){
			this.maskedObjectClasses.remove(mask);
		}
	}


	/**
	 * Clears all state variable masks.
	 */
	public void clearAllAttributeMasks(){
		this.maskedVariables.clear();
	}


	/**
	 * Clears all object class masks.
	 */
	public void clearAllObjectClassMasks(){
		this.maskedObjectClasses.clear();
	}

	public Set<Object> getMaskedVariables() {
		return maskedVariables;
	}

	public Set<String> getMaskedObjectClasses() {
		return maskedObjectClasses;
	}


	@Override
	protected int computeOOHashCode(OOState s) {
		List<Integer> hashCodes = new ArrayList<Integer>(s.numTotalObjects());
		List<ObjectInstance> objects = s.objects();
		for(int i = 0; i < s.numTotalObjects(); i++){
			ObjectInstance o = objects.get(i);
			if(!this.maskedObjectClasses.contains(o.className())) {
				int oHash = this.computeFlatHashCode(o);
				int classNameHash = o.className().hashCode();
				int nameHash = this.objectIdentifierIndependent() ? 0 : o.name().hashCode();
				int totalHash = oHash + 31 * classNameHash + 31 * 31 * nameHash;
				hashCodes.add(totalHash);
			}
		}

		//sort for invariance to order
		Collections.sort(hashCodes);

		//combine
		HashCodeBuilder hashCodeBuilder = new HashCodeBuilder(17, 31);
		for(int hashCode : hashCodes){
			hashCodeBuilder.append(hashCode);
		}

		return hashCodeBuilder.toHashCode();
	}

	@Override
	protected void appendHashCodeForValue(HashCodeBuilder hashCodeBuilder, Object key, Object value) {
		if(!this.maskedVariables.contains(key)){ //only consider variables that have not been masked
			super.appendHashCodeForValue(hashCodeBuilder, key, value);
		}
	}

	@Override
	protected boolean identifierIndependentEquals(OOState s1, OOState s2) {
		if(s1 == s2){
			return true;
		}

		Set<String> matchedObjects = new HashSet<String>();
		for(Map.Entry<String, List<ObjectInstance>> e1 : OOStateUtilities.objectsByClass(s1).entrySet()){

			String oclass = e1.getKey();

			if(this.maskedObjectClasses.contains(oclass)){
				continue;
			}

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

	@Override
	protected boolean identifierDependentEquals(OOState s1, OOState s2) {

		if (s1 == s2) {
			return true;
		}

		Set<String> matchedObjects = new HashSet<String>();
		for(Map.Entry<String, List<ObjectInstance>> e1 : OOStateUtilities.objectsByClass(s1).entrySet()){

			String oclass = e1.getKey();

			if(this.maskedObjectClasses.contains(oclass)){
				continue;
			}

			List<ObjectInstance> objects = e1.getValue();

			List<ObjectInstance> oobjects = s2.objectsOfClass(oclass);
			if(objects.size() != oobjects.size()){
				return false;
			}

			for(ObjectInstance o : objects){
				ObjectInstance oo = s2.object(o.name());
				if(oo == null || !flatStatesEqual(o, oo)){
					return false;
				}
			}

		}

		return true;
	}

	@Override
	protected boolean valuesEqual(Object key, Object v1, Object v2) {
		if(this.maskedVariables.contains(key)){
			return true;
		}
		return super.valuesEqual(key, v1, v2);
	}

}
