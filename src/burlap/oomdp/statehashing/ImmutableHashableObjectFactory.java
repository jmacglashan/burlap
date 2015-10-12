package burlap.oomdp.statehashing;

import java.util.Iterator;
import java.util.List;

import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.objects.ImmutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.values.Value;

/**
 * This is a HashingFactory specifically for ImmutableObjects. By caching hashes with ObjectInstances themselves,
 * reusing ImmutableObjectInstances between states, allows for reduced needs for caching.
 * @author brawner
 *
 */
public class ImmutableHashableObjectFactory implements HashableObjectFactory {
	private final SimpleHashableStateFactory stateFactory;
	public ImmutableHashableObjectFactory(SimpleHashableStateFactory stateFactory) {
		this.stateFactory = stateFactory;
	}

	@Override
	public HashableObject hashObject(ObjectInstance object) {
		if (!(object instanceof ImmutableObjectInstance)) {
			throw new RuntimeException("This object hashing factory must be used for ImmutableObjectInstances");
		}
		
		ImmutableObjectInstance immObj = (ImmutableObjectInstance)object;
		
		if (!immObj.isHashed()) {
			int code = ImmutableHashableObjectFactory.this.computeHashCode(object);
			return new ImmutableHashableObject(immObj.setHashCode(code));
		}
		return new ImmutableHashableObject(immObj);
	}
	
	public boolean objectValuesEqual(ImmutableObjectInstance o1, ImmutableObjectInstance o2){
		if (o1 == o2) {
			return true;
		}
		ObjectClass oc = o1.getObjectClass();
		if (oc != o2.getObjectClass()) {
			return false;
		}
		Iterator<Value> vIt1 = o1.iterator();
		Iterator<Value> vIt2 = o2.iterator();
		while (vIt1.hasNext()) {
			if (!this.stateFactory.valuesEqual(vIt1.next(), vIt2.next())) {
				return false;
			}
		}

		return true;
	}
	
	protected int computeHashCode(ObjectInstance object) {
		return this.stateFactory.computeHashCode(object);
	}
	
	@Override
	public HashableValueFactory getValueHashFactory() {
		return null;
	}
	
	public class ImmutableHashableObject extends HashableObject {

		public ImmutableHashableObject(ImmutableObjectInstance source) {
			super(source);
		}
		
		@Override
		public int hashCode() {
			return this.source.hashCode();
		}
		
		public ImmutableObjectInstance getObjectInstance() {
			return (ImmutableObjectInstance)this.source;
		}

	}

}
