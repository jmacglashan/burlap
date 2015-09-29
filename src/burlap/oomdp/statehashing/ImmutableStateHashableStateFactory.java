package burlap.oomdp.statehashing;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;

public class ImmutableStateHashableStateFactory extends SimpleHashableStateFactory {

	public ImmutableStateHashableStateFactory(boolean identifierIndependent) {
		super(identifierIndependent, true);
	}
	
	/**
	 * Evaluates whether two states are equal when equality is independent of object identifiers/names being equal
	 * @param s1 the first {@link State} to compare
	 * @param s2 the second {@link State} to compare
	 * @return true if s1 = s2; false otherwise
	 */
	@Override
	protected boolean identifierIndependentEquals(State s1, State s2){

		if(s1.numTotalObjects() != s2.numTotalObjects()){
			return false;
		}

		Set<String> matchedObjects = new HashSet<String>();
		List<List<ObjectInstance>> allObjects1 = s1.getAllObjectsByClass();
		List<List<ObjectInstance>> allObjects2 = s2.getAllObjectsByClass();
		
		if (allObjects1.size() != allObjects2.size()) {
			return false;
		}
		for (int i = 0; i < allObjects1.size(); ++i) {
			List<ObjectInstance> objects1 = allObjects1.get(i);
			if (objects1.size() == 0) {
				continue;
			}
			
			String className1 = objects1.get(0).getClassName();
			
			List<ObjectInstance> objects2 = allObjects2.get(i);
			if (objects2.size() == 0 || className1.equals(objects2.get(0).getClassName())) {
				objects2 = s2.getObjectsOfClass(className1);
			}
			
			if (objects1.size() != objects2.size()) {
				return false;
			}
			
			for(ObjectInstance o : objects1){
				boolean foundMatch = false;
				for(ObjectInstance oo : objects2){
					String ooname = oo.getName();
					if(matchedObjects.contains(ooname)){
						continue;
					}
					if(objectValuesEqual(o, oo)){
						foundMatch = true;
						matchedObjects.add(ooname);
						break;
					}
				}
				if(!foundMatch){
					return false;
				}
			}
			
		}

		return true;

	}

	/**
	 * Evaluates whether two states are equal when equality depends on object identifiers/names being equal.
	 * @param s1 the first {@link State} to compare
	 * @param s2 the second {@link State} to compare
	 * @return true if s1 = s2; false otherwise
	 */
	@Override
	protected boolean identifierDependentEquals(State s1, State s2){

		if(s1.numTotalObjects() != s2.numTotalObjects()){
			return false;
		}

		List<ObjectInstance> theseObjects = s1.getAllObjects();
		if(theseObjects.size() != s2.numTotalObjects()){
			return false;
		}
		List<ObjectInstance> thoseObjects = s2.getAllObjects();
		for (int i = 0; i < theseObjects.size(); ++i) {
			ObjectInstance ob1 = theseObjects.get(i);
			ObjectInstance ob2 = thoseObjects.get(i);
			String name = ob1.getName();
			
			if (!name.equals(ob2.getName())) {
				ob2 = s2.getObject(name);
				if (ob2 == null) {
					return false;
				}
			}
			if (!objectValuesEqual(ob1, ob2)){
				return false;
			}
		}
		/*for(ObjectInstance ob : theseObjects){
			ObjectInstance oByName = s2.getObject(ob.getName());
			if(oByName == null){
				return false;
			}
			if(!objectValuesEqual(ob, oByName)){
				return false;
			}
		}*/

		return true;

	}

}
