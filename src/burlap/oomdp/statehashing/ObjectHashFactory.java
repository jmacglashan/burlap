package burlap.oomdp.statehashing;

import burlap.oomdp.core.objects.ObjectInstance;

public interface ObjectHashFactory {

	HashableObject hashObject(ObjectInstance object);
	ValueHashFactory getValueHashFactory();
}
