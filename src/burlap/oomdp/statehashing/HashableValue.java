package burlap.oomdp.statehashing;

import burlap.oomdp.core.values.Value;

public class HashableValue {
	private final HashableValueFactory hashingFactory;
	private final Value value;
	private final int hashCode;
	
	public HashableValue(Value value, HashableValueFactory hashingFactory, int hashCode) {
		this.value = value;
		this.hashingFactory = hashingFactory;
		this.hashCode = hashCode;
	}
	
	public HashableValueFactory getHashFactory() {
		return this.hashingFactory;
	}
	
	public Value getValue() {
		return this.value;
	}
	
	@Override
	public int hashCode() {
		return this.hashCode;
	}
}
