package burlap.oomdp.statehashing;

import burlap.oomdp.core.objects.OldObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.core.values.Value;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.*;

/**
 * This class produces {@link burlap.oomdp.statehashing.HashableState} instances in which the hash code and equality
 * of the states ignores either {@link OldObjectInstance} belonging to
 * specific {@link burlap.oomdp.core.ObjectClass} or value assignments for specific {@link burlap.oomdp.core.Attribute}s.
 * You can specify which attributes and object classes to ignore through an attribute name mask and an object class name
 * mask. There are a variety of methods for manipulating the masks.
 * <p>
 * This class extends {@link burlap.oomdp.statehashing.SimpleHashableStateFactory}, which means it can be toggled to
 * to be object identifier/name independent or dependent and can be set to use {@link burlap.oomdp.statehashing.HashableState}
 * instances that hash their hash code or not. See the {@link burlap.oomdp.statehashing.SimpleHashableStateFactory}
 * class documentation for more information on those features.
 *
 * @author James MacGlashan.
 */
public class MaskedHashableStateFactory extends SimpleHashableStateFactory {

	protected Set<String> maskedAttributes = new HashSet<String>();
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
	 * Initializes with no object class or attribute masks.
	 * @param identifierIndependent if true then state evaluations are object identifier independent; if false then dependent.
	 * @param useCached if true then the hash code for each produced {@link burlap.oomdp.statehashing.HashableState} will be cached; if false then they will not be cached.
	 */
	public MaskedHashableStateFactory(boolean identifierIndependent, boolean useCached) {
		super(identifierIndependent, useCached);
	}


	/**
	 * Initializes with a specified attribute or object class mask.
	 * @param identifierIndependent if true then state evaluations are object identifier independent; if false then dependent.
	 * @param useCached if true then the hash code for each produced {@link burlap.oomdp.statehashing.HashableState} will be cached; if false then they will not be cached.
	 * @param maskNamesAreForAttributes whether the specified masks are masks for attributes or object classes. True for attributes, false for object classes.
	 * @param masks the names of the {@link burlap.oomdp.core.Attribute}s or {@link burlap.oomdp.core.ObjectClass} that will be masks (ignored from state hashing and equality checks)
	 */
	public MaskedHashableStateFactory(boolean identifierIndependent, boolean useCached, boolean maskNamesAreForAttributes, String... masks) {
		super(identifierIndependent, useCached);
		if(maskNamesAreForAttributes){
			for(String mask : masks){
				this.maskedAttributes.add(mask);
			}
		}
		else{
			for(String mask : masks){
				this.maskedObjectClasses.add(mask);
			}
		}
	}

	/**
	 * Adds masks for {@link burlap.oomdp.core.Attribute}s
	 * @param masks the names of the {@link burlap.oomdp.core.Attribute}s to mask.
	 */
	public void addAttributeMasks(String...masks){
		for(String mask : masks){
			this.maskedAttributes.add(mask);
		}
	}

	/**
	 * Adds masks for  {@link burlap.oomdp.core.ObjectClass}s
	 * @param masks the names of the {@link burlap.oomdp.core.ObjectClass}s to mask.
	 */
	public void addObjectClassMasks(String...masks){
		for(String mask : masks){
			this.maskedObjectClasses.add(mask);
		}
	}


	/**
	 * Removes {@link burlap.oomdp.core.Attribute} masks.
	 * @param masks the names of the {@link burlap.oomdp.core.Attribute}s that will no longer be masked.
	 */
	public void removeAttributeMasks(String...masks){
		for(String mask : masks){
			this.maskedAttributes.remove(mask);
		}
	}


	/**
	 * Removes {@link burlap.oomdp.core.ObjectClass} masks.
	 * @param masks the names of the {@link burlap.oomdp.core.ObjectClass}s that will no longer be masked.
	 */
	public void removeObjectClassMasks(String...masks){
		for(String mask : masks){
			this.maskedObjectClasses.remove(mask);
		}
	}


	/**
	 * Clears all {@link burlap.oomdp.core.Attribute} masks.
	 */
	public void clearAllAttributeMasks(){
		this.maskedAttributes.clear();
	}


	/**
	 * Clears all {@link burlap.oomdp.core.ObjectClass} masks.
	 */
	public void clearAllObjectClassMasks(){
		this.maskedObjectClasses.clear();
	}

	public Set<String> getMaskedAttributes() {
		return maskedAttributes;
	}

	public Set<String> getMaskedObjectClasses() {
		return maskedObjectClasses;
	}


	@Override
	protected int computeHashCode(State s) {
		List<OldObjectInstance> objects = s.getAllObjects();
		List<Integer> hashCodes = new ArrayList<Integer>(objects.size());
		for(OldObjectInstance o : objects){
			if(!this.maskedObjectClasses.contains(o.getClassName())){
				hashCodes.add(computeHashCode(o));
			}
		}
		//sort for invariance to object appearance order
		Collections.sort(hashCodes);
		HashCodeBuilder hashCodeBuilder = new HashCodeBuilder(17, 31);
		for(int code : hashCodes){
			hashCodeBuilder.append(code);
		}
		int code = hashCodeBuilder.toHashCode();

		return code;
	}

	@Override
	protected int computeHashCode(OldObjectInstance o) {
		HashCodeBuilder hashCodeBuilder = new HashCodeBuilder(17, 31);
		if(!this.identifierIndependent){
			hashCodeBuilder.append(o.getName());
		}

		List<Value> values = o.getValues();
		for(Value v : values){
			if(!this.maskedAttributes.contains(v.attName())) {
				this.appendHashcodeForValue(hashCodeBuilder, v);
			}
		}


		return hashCodeBuilder.toHashCode();
	}

	@Override
	protected boolean identifierIndependentEquals(State s1, State s2) {
		if(s1.numTotalObjects() != s2.numTotalObjects() && this.maskedObjectClasses.isEmpty()){
			return false;
		}

		Set<String> matchedObjects = new HashSet<String>();
		for(List<OldObjectInstance> objects : s1.getAllObjectsByClass()){

			String oclass = objects.get(0).getClassName();
			if(this.maskedObjectClasses.contains(oclass)){
				continue;
			}
			List <OldObjectInstance> oobjects = s2.getObjectsOfClass(oclass);
			if(objects.size() != oobjects.size()){
				return false;
			}

			for(OldObjectInstance o : objects){
				boolean foundMatch = false;
				for(OldObjectInstance oo : oobjects){
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

	@Override
	protected boolean identifierDependentEquals(State s1, State s2) {
		if(s1.numTotalObjects() != s2.numTotalObjects() && this.maskedObjectClasses.isEmpty()){
			return false;
		}

		List<OldObjectInstance> theseObjects = s1.getAllObjects();
		if(theseObjects.size() != s2.numTotalObjects()){
			return false;
		}
		for(OldObjectInstance ob : theseObjects){
			if(this.maskedObjectClasses.contains(ob.getClassName())){
				continue;
			}
			OldObjectInstance oByName = s2.getObject(ob.getName());
			if(oByName == null){
				return false;
			}
			if(!objectValuesEqual(ob, oByName)){
				return false;
			}
		}

		return true;
	}

	@Override
	protected boolean objectValuesEqual(OldObjectInstance o1, OldObjectInstance o2) {
		for(Value v : o1.getValues()){
			if(this.maskedAttributes.contains(v.attName())){
				continue;
			}
			Value ov = o2.getValueForAttribute(v.attName());
			if(!valuesEqual(v, ov)){
				return false;
			}
		}
		return true;
	}
}
