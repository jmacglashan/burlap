package burlap.oomdp.statehashing;

import burlap.oomdp.core.objects.ImmutableObjectInstance;
import burlap.oomdp.core.states.FixedSizeImmutableState;
import burlap.oomdp.core.states.ImmutableStateInterface;
import burlap.oomdp.core.states.State;

import java.util.BitSet;
import java.util.Iterator;

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
	 * Sets all masking value of the provided object classes.
	 * A value of true, signifies an object will be included in hashing/equality testing
	 * @param objectClassNames the object class names to mask
	 */
	@Override
	public void setObjectClassMask(boolean value, String ...objectClassNames) {
		super.setObjectClassMask(value, objectClassNames);
		for (String objectClassName : objectClassNames) {
			for (int i = 0; i < this.initialState.numTotalObjects(); i++) {
				if (this.initialState.getObject(i).getObjectClass().name.equals(objectClassName)) {
					this.objectMask.set(i, value);
				}
			}
		}
	}
	
	/**
	 * Sets all masking value of the provided objects.
	 * A value of true, signifies an object will be included in hashing/equality testing
	 * @param objectNames the object names to mask
	 */
	@Override
	public void setObjectMask(boolean value, String ... objectNames) {
		super.setObjectMask(value, objectNames);
		for (String objectName : objectNames) {
			for (int i = 0; i < this.initialState.numTotalObjects(); i++) {
				if (this.initialState.getObject(i).getName().equals(objectName)) {
					this.objectMask.set(i, value);
				}
			}
		}
	}
	
	@Override
	protected boolean statesEqual(State s1, State s2) {
		if (this.identifierIndependent) {
			return super.statesEqual(s1, s2);
		}
		
		int size1 = s1.numTotalObjects();
		if(size1 != s2.numTotalObjects()){
			return false;
		}
		
		ImmutableStateInterface iS1 = this.getImmutableState(s1);
		ImmutableStateInterface iS2 = this.getImmutableState(s2);
		
		if (!iS1.isHashed() || !iS2.isHashed()) {
			throw new RuntimeException("These states should be hashed for this equality comparison");
		}
		
		return identifierDependentEquals(iS1, iS2);
	}

	/**
	 * Because the objects are assumed to be in a fixed order. If the equality comparison is name dependent
	 * you don't need to check equality among any other objects, just the ones used in the comparison.
	 * @param s1 first {@link burlap.oomdp.core.states.ImmutableState} to compare
	 * @param s2 second {@link burlap.oomdp.core.states.ImmutableState} o compare
	 * @return true or false
	 */
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
			if (!ob1.equals(ob2)) {
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
