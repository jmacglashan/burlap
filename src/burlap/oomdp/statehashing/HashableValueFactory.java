package burlap.oomdp.statehashing;

import burlap.oomdp.core.values.Value;

public interface HashableValueFactory {

	HashableValue hashValue(Value value);
}
