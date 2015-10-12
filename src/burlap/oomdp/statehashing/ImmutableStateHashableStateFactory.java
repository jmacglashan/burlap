package burlap.oomdp.statehashing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import burlap.oomdp.core.objects.ImmutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.ImmutableStateInterface;
import burlap.oomdp.core.states.State;
import burlap.oomdp.statehashing.ImmutableHashableObjectFactory.ImmutableHashableObject;

import com.google.common.collect.ImmutableList;

/**
 * This Hashing factory works for states that implement the ImmutableStateInterface. It caches the value by default, and 
 * also will check object equality through the == comparison, since ImmutableStates may share the same objects.
 * 
 * @author brawner
 *
 */
public class ImmutableStateHashableStateFactory extends SimpleHashableStateFactory {
	
	protected final ImmutableHashableObjectFactory objectHashingFactory;
	public ImmutableStateHashableStateFactory(boolean identifierIndependent) {
		super(identifierIndependent, true);
		this.objectHashingFactory = new ImmutableHashableObjectFactory(this);
	}
	
	@Override
	public HashableState hashState(State s) {
		if (s instanceof ImmutableHashableState) {
			return (ImmutableHashableState)s;
		}
		
		if (!(s instanceof ImmutableStateInterface)) {
			throw new RuntimeException(s.getClass().toString() + " is not an immutable state type.");
		}
		
		ImmutableStateInterface immState = (ImmutableStateInterface)s;
		List<ImmutableObjectInstance> hashed = new ArrayList<ImmutableObjectInstance>(s.numTotalObjects());
		List<Integer> hashes = new ArrayList<Integer>(s.numTotalObjects());
		
		for (ObjectInstance obj : immState){ 
			ObjectInstance hashedObj = this.objectHashingFactory.hashObject(obj);
			hashed.add(((ImmutableHashableObject)hashedObj).getObjectInstance());
			hashes.add(hashedObj.hashCode());
		}
		
		ImmutableList<ImmutableObjectInstance> immList = ImmutableList.copyOf(hashed);
		Collections.sort(hashes);
		return new ImmutableHashableState(immState.replaceAndHash(immList, hashes.hashCode()));
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
		
		ImmutableStateInterface iS1 = this.getImmutableState(s1);
		ImmutableStateInterface iS2 = this.getImmutableState(s2);
		return this.identifierDependentEquals(iS1, iS2);
	}

	protected boolean identifierDependentEquals(ImmutableStateInterface s1, ImmutableStateInterface s2){
		Iterator<ImmutableObjectInstance> it1 = s1.iterator();
		Iterator<ImmutableObjectInstance> it2 = s2.iterator();
		
		while (it1.hasNext()) {
			ImmutableObjectInstance ob1 = it1.next();
			ImmutableObjectInstance ob2 = it2.next();
			
			if (ob1 == ob2) {
				continue;
			}
			
			String name = ob1.getName();
			
			if (!name.equals(ob2.getName())) {
				ObjectInstance obj = s2.getObject(name);
				if (obj == null) {
					return false;
				}
				ob2 = (ImmutableObjectInstance)obj;
				
			}
			if (!this.objectHashingFactory.objectValuesEqual(ob1, ob2)){
				return false;
			}
		}

		return true;
	}
	
	private ImmutableStateInterface getImmutableState(State s) {
		if (s instanceof ImmutableHashableState) {
			return ((ImmutableHashableState)s).getImmutableState();
		} else if (s instanceof ImmutableStateInterface) {
			return (ImmutableStateInterface)s;
		} else {
			throw new RuntimeException("State of type " + s.getClass().toString() + " does not implement the ImmutableStateInterface interface");
		}
	}
	
	public class ImmutableHashableState extends HashableState {
		public ImmutableHashableState(ImmutableStateInterface s) {
			super(s);
		}

		@Override
		public State copy() {
			return this;
		}
		
		public ImmutableStateInterface getImmutableState() {
			return (ImmutableStateInterface)this.s;
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
					ImmutableStateHashableStateFactory.this.statesEqual(this.s, other.s);
		}

		@Override
		public int hashCode() {
			return this.s.hashCode();
		}

	}

}
