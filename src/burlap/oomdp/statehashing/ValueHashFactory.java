package burlap.oomdp.statehashing;

import burlap.oomdp.core.values.Value;

public interface ValueHashFactory {

	ValueHashTuple hashValue(Value value);
}
