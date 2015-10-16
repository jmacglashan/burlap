package burlap.oomdp.statehashing;

import burlap.oomdp.core.objects.ObjectInstance;

public interface HashableObjectFactory {

	HashableObject hashObject(ObjectInstance object);
	HashableValueFactory getValueHashFactory();
}
