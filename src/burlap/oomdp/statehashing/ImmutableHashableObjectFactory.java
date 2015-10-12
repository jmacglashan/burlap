package burlap.oomdp.statehashing;

import burlap.oomdp.core.objects.ImmutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;

public class ImmutableHashableObjectFactory implements ObjectHashFactory {
	private final SimpleHashableStateFactory stateFactory;
	public ImmutableHashableObjectFactory(SimpleHashableStateFactory stateFactory) {
		this.stateFactory = stateFactory;
	}

	@Override
	public HashableObject hashObject(ObjectInstance object) {
		ImmutableObjectInstance immObj = null;
		if (object instanceof ImmutableObjectInstance) {
			immObj = (ImmutableObjectInstance)object;
		} else {
			immObj = new ImmutableObjectInstance(object);
		}
		if (!immObj.isHashed()) {
			int code = ImmutableHashableObjectFactory.this.computeHashCode(object);
			return new ImmutableHashableObject(immObj.setHashCode(code));
		}
		return new ImmutableHashableObject(immObj);
	}
	
	protected int computeHashCode(ObjectInstance object) {
		return this.stateFactory.computeHashCode(object);
	}
	
	@Override
	public ValueHashFactory getValueHashFactory() {
		// TODO Auto-generated method stub
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
