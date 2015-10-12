package burlap.oomdp.statehashing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.objects.ImmutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.ImmutableFixedSizeState;
import burlap.oomdp.core.states.State;
import burlap.oomdp.core.values.Value;
import burlap.oomdp.statehashing.ImmutableHashableObjectFactory.ImmutableHashableObject;

public class ImmutableFixedObjectStateHashableStateFactory extends SimpleHashableStateFactory {
	private ImmutableHashableObjectFactory objectHashingFactory;
	private final BitSet objectMask;
	private final ImmutableFixedSizeState initialState;
	
	public ImmutableFixedObjectStateHashableStateFactory(boolean identifierIndependent, ImmutableFixedSizeState initialState) {
		super(identifierIndependent, true);
		this.objectHashingFactory = new ImmutableHashableObjectFactory(this);
		this.objectMask = new BitSet(initialState.numTotalObjects());
		this.objectMask.set(0, initialState.numTotalObjects(), true);
		this.initialState = initialState;
	}
	
	public void setObjectClassMask(String objectClassName) {
		this.setObjectClassMask(objectClassName, true);
	}
	
	public void setObjectClassMask(String objectClassName, boolean value) {
		for (int i = 0; i < this.initialState.numTotalObjects(); i++) {
			if (this.initialState.getObject(i).getObjectClass().name.equals(objectClassName)) {
				this.objectMask.set(i, value);
			}
		}
	}
	
	public void setObjectMask(String objectName, boolean value) {
		for (int i = 0; i < this.initialState.numTotalObjects(); i++) {
			if (this.initialState.getObject(i).getName().equals(objectName)) {
				this.objectMask.set(i, value);
			}
		}
	}
	
	@Override
	public HashableState hashState(State s) {
		if (s instanceof ImmutableHashableState) {
			return (ImmutableHashableState)s;
		}
		ImmutableFixedSizeState sTimm = (ImmutableFixedSizeState)s;
		int size = sTimm.numTotalObjects();
		List<ImmutableObjectInstance> hashed = new ArrayList<ImmutableObjectInstance>(size);
		for (int i = this.objectMask.nextSetBit(0); i >= 0; i = this.objectMask.nextSetBit(i+1)) {
			ObjectInstance obj = sTimm.getObject(i);
			ObjectInstance hashedObj = this.objectHashingFactory.hashObject(obj);
			hashed.add(((ImmutableHashableObject)hashedObj).getObjectInstance());
		}
		//Arrays.sort(hashCodes);
		
		ImmutableList<ImmutableObjectInstance> immList = ImmutableList.copyOf(hashed);
		return new ImmutableHashableState(sTimm.replaceAndHash(immList, immList.hashCode()));
	}
	

	@Override
	protected int computeHashCode(State s){
		return this.hashState(s).hashCode();
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

	/**
	 * Evaluates whether two states are equal when equality depends on object identifiers/names being equal.
	 * @param s1 the first {@link State} to compare
	 * @param s2 the second {@link State} to compare
	 * @return true if s1 = s2; false otherwise
	 */
	@Override
	protected boolean identifierDependentEquals(State s1, State s2){
		int size1 = s1.numTotalObjects();
		if(size1 != s2.numTotalObjects()){
			return false;
		}
		
		ImmutableFixedSizeState iS1 = this.getImmutableState(s1);
		ImmutableFixedSizeState iS2 = this.getImmutableState(s2);
		return this.identifierDependentEquals(iS1, iS2);
//		for (int i = 0; i < size1; ++i) {		
//			ImmutableObjectInstance ob1 = (ImmutableObjectInstance)iS1.getObject(i);
//			ImmutableObjectInstance ob2 = (ImmutableObjectInstance)iS2.getObject(i);
//			String name = ob1.getName();
//			
//			if (!name.equals(ob2.getName())) {
//				ob2 = (ImmutableObjectInstance)s2.getObject(name);
//				if (ob2 == null) {
//					return false;
//				}
//			}
//			if (!this.objectValuesEqual(ob1, ob2)){
//				return false;
//			}
//		}
//
//		return true;
	}

	protected boolean identifierDependentEquals(ImmutableFixedSizeState s1, ImmutableFixedSizeState s2){
		for (int i = this.objectMask.nextSetBit(0); i >= 0; i = this.objectMask.nextSetBit(i+1)) {
			ImmutableObjectInstance ob1 = (ImmutableObjectInstance)s1.getObject(i);
			ImmutableObjectInstance ob2 = (ImmutableObjectInstance)s2.getObject(i);
			String name = ob1.getName();
			
			if (!name.equals(ob2.getName())) {
				return false;
			}
			if (!this.objectValuesEqual(ob1, ob2)){
				return false;
			}
		}

		return true;
	}
	
	protected boolean objectValuesEqual(ImmutableObjectInstance o1, ImmutableObjectInstance o2){
		ObjectClass oc = o1.getObjectClass();
		if (oc != o2.getObjectClass()) {
			return false;
		}
		List<Value> values1 = o1.getValues();
		List<Value> values2 = o2.getValues();
		int size = values1.size();
		for (int i = 0; i < size; i++) {
			if (!valuesEqual(values1.get(i), values2.get(i))) {
				return false;
			}
		}

		return true;
	}
	
	private ImmutableFixedSizeState getImmutableState(State s) {
		if (s instanceof ImmutableHashableState) {
			return ((ImmutableHashableState)s).getImmutableState();
		} else if (s instanceof ImmutableFixedSizeState) {
			return (ImmutableFixedSizeState)s;
		} else {
			return new ImmutableFixedSizeState(s);
		}
	}
	
	private ImmutableObjectInstance getImmutableObject(ObjectInstance obj) {
		if (obj instanceof ImmutableHashableObject) {
			return ((ImmutableHashableObject)obj).getObjectInstance();
		} else if (obj instanceof ImmutableObjectInstance) {
			return (ImmutableObjectInstance)obj;
		} 
		return null;
	}
	
	public class ImmutableHashableState extends HashableState {
		public ImmutableHashableState(ImmutableFixedSizeState s) {
			super(s);
		}

		@Override
		public State copy() {
			return this;
		}
		
		public ImmutableFixedSizeState getImmutableState() {
			return (ImmutableFixedSizeState)this.s;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			
			if (!(obj instanceof ImmutableHashableState)) {
				return false;
			}
			
			ImmutableHashableState other = (ImmutableHashableState)obj;
			return other.hashCode() == this.hashCode() && 
					ImmutableFixedObjectStateHashableStateFactory.this.statesEqual(this.s, other.s);
		}

		@Override
		public int hashCode() {
			return this.s.hashCode();
		}

	}

}
