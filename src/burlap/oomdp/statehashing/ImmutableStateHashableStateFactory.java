package burlap.oomdp.statehashing;

import burlap.oomdp.core.objects.ImmutableObjectInstance;
import burlap.oomdp.core.objects.OldObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.statehashing.ImmutableHashableObjectFactory.ImmutableHashableObject;
import com.google.common.collect.ImmutableList;
import gnu.trove.list.array.TIntArrayList;

import java.util.*;

/**
 * This Hashing factory works for states that implement the ImmutableStateInterface. It caches the value by default, and 
 * also will check object equality through the == comparison, since ImmutableStates may share the same objects.
 * 
 * @author brawner
 *
 */
public class ImmutableStateHashableStateFactory extends SimpleHashableStateFactory {
	
	protected final ImmutableHashableObjectFactory objectHashingFactory;
	protected final Set<String> maskedObjectClasses;
	protected final Set<String> maskedObjects;
	
	public ImmutableStateHashableStateFactory(boolean identifierIndependent) {
		super(identifierIndependent, true);
		this.objectHashingFactory = new ImmutableHashableObjectFactory(this, identifierIndependent);

		this.maskedObjectClasses = new HashSet<String>();
		this.maskedObjects = new HashSet<String>();
	}
	
	/**
	 * Sets all objects of the provided object class to not be hashed, or used in an equality comparison.
	 * @param objectClasses the object class names to mask
	 */
	public void setObjectClassMask(String ... objectClasses) {
		this.setObjectClassMask(false, objectClasses);
	}
	
	/**
	 * Sets all masking value of the provided object classes.
	 * A value of true, signifies an object will be included in hashing/equality testing
	 * @param objectClassNames the object class names to mask
	 */
	public void setObjectClassMask(boolean value, String ...objectClassNames) {
		List<String> list = Arrays.asList(objectClassNames);
		if (value) {
			this.maskedObjectClasses.removeAll(list);
		} else {
			this.maskedObjectClasses.addAll(list);
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
	 * @param objectNames the object names to mask
	 */
	public void setObjectMask(boolean value, String ... objectNames) {
		List<String> list = Arrays.asList(objectNames);
		if (value) {
			this.maskedObjects.removeAll(list);
		} else {
			this.maskedObjects.addAll(list);
		}
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
		TIntArrayList hashes = new TIntArrayList(s.numTotalObjects());
		
		for (ImmutableObjectInstance obj : immState){ 
			ImmutableHashableObject hashedObj = this.objectHashingFactory.hashObject(obj);
			hashed.add(hashedObj.getObjectInstance());
			if (!this.isObjectMasked(obj)) {
				hashes.add(hashedObj.hashCode());
			}
		}
		
		ImmutableList<ImmutableObjectInstance> immList = ImmutableList.copyOf(hashed);
		hashes.sort();
		return new ImmutableHashableState(immState.replaceAndHash(immList, hashes.hashCode()));
	}
	
	protected boolean isObjectMasked(OldObjectInstance obj) {
		return this.maskedObjectClasses.contains(obj.getClassName()) ||
				this.maskedObjects.contains(obj.getName());
	}
	
	@Override
	protected int computeHashCode(State s){
		return this.hashState(s).hashCode();
	}
	
	@Override
	protected boolean statesEqual(State s1, State s2) {
		int size1 = s1.numTotalObjects();
		if(size1 != s2.numTotalObjects()){
			return false;
		}
		
		ImmutableStateInterface iS1 = this.getImmutableState(s1);
		ImmutableStateInterface iS2 = this.getImmutableState(s2);
		
		if (!iS1.isHashed() || !iS2.isHashed()) {
			throw new RuntimeException("These states should be hashed this equality comparison");
		}
		
		Set<OldObjectInstance> set1 = this.prepareSet(iS1);
		Set<OldObjectInstance> set2 = this.prepareSet(iS2);
		return set1.equals(set2);
	}
	
	protected Set<OldObjectInstance> prepareSet(ImmutableStateInterface s) {
		if (this.maskedObjectClasses.isEmpty() && this.maskedObjects.isEmpty()) {
			return new HashSet<OldObjectInstance>(s.getImmutableObjects());
		}
		Set<OldObjectInstance> set = new HashSet<OldObjectInstance>(s.numTotalObjects() * 2);
		
		if (this.maskedObjects.isEmpty()) {
			List<List<OldObjectInstance>> objectsByClass = s.getAllObjectsByClass();
			for (List<OldObjectInstance> objects : objectsByClass) {
				if (objects.isEmpty()) {
					continue;
				}
				String obClass = objects.get(0).getClassName();
				if (this.maskedObjectClasses.contains(obClass)) {
					continue;
				}
				set.addAll(objects);	
			}
		}
		
		for (ImmutableObjectInstance obj : s) {
			if (!this.maskedObjectClasses.contains(obj.getClassName()) &&
					!this.maskedObjects.contains(obj.getName())) {
				set.add(obj);
			}
		}
		return set;
	}
	
	protected ImmutableStateInterface getImmutableState(State s) {
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
			if (other.hashCode() != this.hashCode()) {
				return false;
			}
			return ImmutableStateHashableStateFactory.this.statesEqual(this.s, other.s);
		}

		@Override
		public int hashCode() {
			return this.s.hashCode();
		}

	}

}
