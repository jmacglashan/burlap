package burlap.mdp.core.oo.state.generic;

import burlap.mdp.core.oo.state.*;
import burlap.mdp.core.oo.state.exceptions.UnknownObjectException;
import burlap.mdp.core.state.MutableState;
import burlap.mdp.core.state.annotations.ShallowCopyState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author James MacGlashan.
 */
@ShallowCopyState
public class GenericOOState implements MutableOOState {


	protected Map<String, List<ObjectInstance>> objectsByClass = new HashMap<String, List<ObjectInstance>>();
	protected Map<String, ObjectInstance> objectsMap = new HashMap<String, ObjectInstance>();

	public GenericOOState() {
	}

	public GenericOOState(OOState srcOOState){
		for(ObjectInstance o : srcOOState.objects()){
			this.addObject(o);
		}
	}

	public GenericOOState(ObjectInstance...objects){
		for(ObjectInstance o : objects){
			this.addObject(o);
		}
	}

	@Override
	public List<Object> variableKeys() {
		return OOStateUtilities.flatStateKeys(this);
	}

	@Override
	public Object get(Object variableKey) {
		return OOStateUtilities.get(this, variableKey);
	}

	@Override
	public MutableState set(Object variableKey, Object value) {

		OOVariableKey key = OOStateUtilities.generateKey(variableKey);
		ObjectInstance ob = this.touch(key.obName);
		if(ob == null){
			throw new UnknownObjectException(key.obName);
		}
		if(!(ob instanceof MutableState)){
			throw new RuntimeException("Cannot set value for object " + ob.name() + " because it does not implement MutableState");
		}
		((MutableState)ob).set(key.obVarKey, value);

		return this;
	}

	@Override
	public GenericOOState copy() {
		return new GenericOOState(this);
	}

	@Override
	public MutableOOState addObject(ObjectInstance o) {
		this.removeObject(o.name());
		List<ObjectInstance> obs = this.getOrGenerateObjectClassList(o.className());
		obs.add(o);
		this.objectsMap.put(o.name(), o);

		return this;
	}

	@Override
	public MutableOOState removeObject(String oname) {

		ObjectInstance stored = this.objectsMap.get(oname);
		if(stored != null){
			this.objectsMap.remove(oname);
			List<ObjectInstance> obs = this.objectsByClass.get(stored.className());
			obs.remove(stored);
			if(obs.size() == 0){
				this.objectsByClass.remove(stored.className());
			}
		}

		return this;
	}

	@Override
	public MutableOOState renameObject(String objectName, String newName) {
		ObjectInstance stored = this.objectsMap.get(objectName);
		if(stored != null){
			this.removeObject(stored.name());
			ObjectInstance copied = stored.copyWithName(newName);
			this.addObject(copied);

		}
		return this;
	}

	@Override
	public int numObjects() {
		return objectsMap.size();
	}

	@Override
	public ObjectInstance object(String oname) {
		return this.objectsMap.get(oname);
	}

	@Override
	public List<ObjectInstance> objects() {
		return new ArrayList<ObjectInstance>(this.objectsMap.values());
	}

	@Override
	public List<ObjectInstance> objectsOfClass(String oclass) {
		List<ObjectInstance> objects = this.objectsByClass.get(oclass);
		if(objects == null){
			return new ArrayList<ObjectInstance>();
		}
		return this.objectsByClass.get(oclass);
	}

	@Override
	public String toString() {
		return OOStateUtilities.ooStateToString(this);
	}


	/**
	 * Copies the object with the given name, and updates its index in this state. Use this method if you
	 * are going to directly modify the {@link ObjectInstance} to ensure that you do not contaminate any states
	 * from which this state was copied.
	 * @param obname the name of the object to be modified.
	 * @return a copy of the {@link ObjectInstance} with the given name. Null if this state contains no object with that name
	 */
	public ObjectInstance touch(String obname){
		ObjectInstance original = this.object(obname);
		if(original != null){
			this.removeObject(obname);

			ObjectInstance next = (ObjectInstance)original.copy();

			List<ObjectInstance> obs = this.getOrGenerateObjectClassList(next.className());
			obs.add(next);
			this.objectsMap.put(next.name(), next);

			return next;

		}

		return null;
	}



	/**
	 * Getter method for underlying data to support serialization.
	 * @return the underlying Map from class names to {@link ObjectInstance} objects for objects in this state.
	 */
	public Map<String, List<ObjectInstance>> getObjectsByClass() {
		return objectsByClass;
	}

	/**
	 * Setter method for underlying data to support serialization
	 * @param objectsByClass the underlying Map from class names to {@link ObjectInstance} objects for objects in this state.
	 */
	public void setObjectsByClass(Map<String, List<ObjectInstance>> objectsByClass) {
		this.objectsByClass = objectsByClass;
	}


	/**
	 * Getter method for underlying data to support serialization.
	 * @return the underlying Map from object names to {@link ObjectInstance} objects for objects in this state.
	 */
	public Map<String, ObjectInstance> getObjectsMap() {
		return objectsMap;
	}

	/**
	 * Setter method for underlying data to support serialization
	 * @param objectsMap the underlying Map from object names to {@link ObjectInstance} objects for objects in this state.
	 */
	public void setObjectsMap(Map<String, ObjectInstance> objectsMap) {
		this.objectsMap = objectsMap;
	}

	protected List<ObjectInstance> getOrGenerateObjectClassList(String className){
		List<ObjectInstance> obs = this.objectsByClass.get(className);
		if(obs == null){
			obs = new ArrayList<ObjectInstance>();
			this.objectsByClass.put(className, obs);
		}
		return obs;
	}

}
