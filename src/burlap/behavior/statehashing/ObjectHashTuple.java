package burlap.behavior.statehashing;

import burlap.oomdp.core.ObjectInstance;

public abstract class ObjectHashTuple {

	private final ObjectInstance object;
	private final ObjectHashFactory hashingFactory;
	private final int hashCode;
	
	public ObjectHashTuple(ObjectInstance object, ObjectHashFactory hashingFactory, int hashCode) {
		this.object = object;
		this.hashingFactory = hashingFactory;
		this.hashCode = hashCode;
	}
	
	public ObjectHashFactory getHashingFactory() {
		return this.hashingFactory;
	}
	
	public ObjectInstance getObject() {
		return this.object;
	}
	
	@Override
	public int hashCode() {
		return this.hashCode;
	}
}
