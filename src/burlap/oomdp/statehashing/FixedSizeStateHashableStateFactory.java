package burlap.oomdp.statehashing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.objects.ImmutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.FixedSizeImmutableState;
import burlap.oomdp.core.states.ImmutableStateInterface;
import burlap.oomdp.core.states.State;
import burlap.oomdp.core.values.Value;
import burlap.oomdp.statehashing.ImmutableHashableObjectFactory.ImmutableHashableObject;

/**
 * This is a hash factory specifically for FixedSizeImmutableStates. It allows you to set a mask, to only hash
 * a subset of objects or object classes. Because it only works for FixedSizeImmutableStates, it hashes objects
 * specifically in the order there exist in the state. It also iterates staight through when doing the state equality
 * comparison, allowing for significant speedups for certain domains.
 * @author brawner
 *
 */
public class FixedSizeStateHashableStateFactory extends ImmutableStateHashableStateFactory {
	private final BitSet objectMask;
	private final FixedSizeImmutableState initialState;
	
	public FixedSizeStateHashableStateFactory(boolean identifierIndependent, FixedSizeImmutableState initialState) {
		super(identifierIndependent);
		this.objectMask = new BitSet(initialState.numTotalObjects());
		this.objectMask.set(0, initialState.numTotalObjects(), true);
		this.initialState = initialState;
	}
	
	/**
	 * Sets all objects of the provided object class to not be hashed, or used in an equality comparison.
	 * @param objectClassName
	 */
	public void setObjectClassMask(String ... objectClasses) {
		this.setObjectClassMask(false, objectClasses);
	}
	
	/**
	 * Sets all masking value of the provided object classes.
	 * A value of true, signifies an object will be included in hashing/equality testing
	 * @param objectClassName
	 */
	public void setObjectClassMask(boolean value, String ...objectClassNames) {
		for (String objectClassName : objectClassNames) {
			for (int i = 0; i < this.initialState.numTotalObjects(); i++) {
				if (this.initialState.getObject(i).getObjectClass().name.equals(objectClassName)) {
					this.objectMask.set(i, value);
				}
			}
		}
	}
	
	/**
	 * Sets all objects provided to not be hashed, or used in an equality comparison
	 * @param objectNames
	 */
	public void setObjectMask(String ... objectNames) {
		this.setObjectMask(false, objectNames);
	}
	
	/**
	 * Sets all masking value of the provided objects.
	 * A value of true, signifies an object will be included in hashing/equality testing
	 * @param objectClassName
	 */
	public void setObjectMask(boolean value, String ... objectNames) {
		for (String objectName : objectNames) {
			for (int i = 0; i < this.initialState.numTotalObjects(); i++) {
				if (this.initialState.getObject(i).getName().equals(objectName)) {
					this.objectMask.set(i, value);
				}
			}
		}
	}
	
	@Override
	public HashableState hashState(State s) {
		if (s instanceof ImmutableHashableState) {
			return (ImmutableHashableState)s;
		}
		
		FixedSizeImmutableState sTimm = (FixedSizeImmutableState)s;
		int size = sTimm.numTotalObjects();
		List<ImmutableObjectInstance> hashed = new ArrayList<ImmutableObjectInstance>(size);
		
		for (int i = this.objectMask.nextSetBit(0); i >= 0; i = this.objectMask.nextSetBit(i+1)) {
			ObjectInstance obj = sTimm.getObject(i);
			ObjectInstance hashedObj = this.objectHashingFactory.hashObject(obj);
			hashed.add(((ImmutableHashableObject)hashedObj).getObjectInstance());
		}
		
		ImmutableList<ImmutableObjectInstance> immList = ImmutableList.copyOf(hashed);
		return new ImmutableHashableState(sTimm.replaceAndHash(immList, immList.hashCode()));
	}
	
	/**
	 * Evaluates whether two states are equal when equality is independent of object identifiers/names being equal
	 * @param s1 the first {@link State} to compare
	 * @param s2 the second {@link State} to compare
	 * @return true if s1 = s2; false otherwise
	 */
	@Override
	protected boolean identifierIndependentEquals(State s1, State s2){

		if(s1.numTotalObjects() != s2.numTotalObjects()){
			return false;
		}

		Set<String> matchedObjects = new HashSet<String>();
		List<List<ObjectInstance>> allObjects1 = s1.getAllObjectsByClass();
		List<List<ObjectInstance>> allObjects2 = s2.getAllObjectsByClass();
		
		if (allObjects1.size() != allObjects2.size()) {
			return false;
		}
		for (int i = 0; i < allObjects1.size(); ++i) {
			
			List<ObjectInstance> objects1 = allObjects1.get(i);
			if (objects1.size() == 0) {
				continue;
			}
			
			String className1 = objects1.get(0).getClassName();
			
			List<ObjectInstance> objects2 = allObjects2.get(i);
			if (objects2.size() == 0 || className1.equals(objects2.get(0).getClassName())) {
				objects2 = s2.getObjectsOfClass(className1);
			}
			
			if (objects1.size() != objects2.size()) {
				return false;
			}
			
			for(ObjectInstance o : objects1){
				
				boolean foundMatch = false;
				for(ObjectInstance oo : objects2){
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
	protected boolean identifierDependentEquals(ImmutableStateInterface s1, ImmutableStateInterface s2){
		if (!(s1 instanceof FixedSizeImmutableState) ||
				!(s2 instanceof FixedSizeImmutableState)) {
			throw new RuntimeException("This state needs to be a FixedSize state");
		}
		FixedSizeImmutableState fs1 = (FixedSizeImmutableState)s1;
		FixedSizeImmutableState fs2 = (FixedSizeImmutableState)s2;
		Iterator<ImmutableObjectInstance> it1 = iterator(fs1, this.objectMask);
		Iterator<ImmutableObjectInstance> it2 = iterator(fs2, this.objectMask);
		
		while (it1.hasNext()) {
			ImmutableObjectInstance ob1 = it1.next();
			ImmutableObjectInstance ob2 = it2.next();
			
			String name = ob1.getName();
			
			if (!name.equals(ob2.getName())) {
				return false;
			}
			if (!this.objectHashingFactory.objectValuesEqual(ob1, ob2)){
				return false;
			}
		}

		return true;
	}
	
	private static Iterator<ImmutableObjectInstance> iterator(final FixedSizeImmutableState state, final BitSet mask) {
		return new Iterator<ImmutableObjectInstance>() {
			int next = mask.nextSetBit(0);
			@Override
			public boolean hasNext() {
				return next >= 0;
			}

			@Override
			public ImmutableObjectInstance next() {
				ImmutableObjectInstance obj = (ImmutableObjectInstance)state.getObject(next);
				next = mask.nextSetBit(next+1);
				return obj;
			}

			@Override
			public void remove() {
				throw new RuntimeException("What are you even doing?");
			}
		};
	}
}
