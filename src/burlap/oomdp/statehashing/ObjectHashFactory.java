package burlap.oomdp.statehashing;

import burlap.oomdp.core.objects.ObjectInstance;

public interface ObjectHashFactory {

	ObjectHashTuple hashObject(ObjectInstance object);
	ValueHashFactory getValueHashFactory();
}
