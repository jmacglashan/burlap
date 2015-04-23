package burlap.behavior.statehashing;

import burlap.oomdp.core.values.Value;

public interface ValueHashFactory {

	ValueHashTuple hashValue(Value value);
}
