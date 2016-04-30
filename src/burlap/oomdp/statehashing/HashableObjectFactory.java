package burlap.oomdp.statehashing;

import burlap.oomdp.core.objects.OldObjectInstance;

public interface HashableObjectFactory {

	HashableObject hashObject(OldObjectInstance object);
	HashableValueFactory getValueHashFactory();
}
