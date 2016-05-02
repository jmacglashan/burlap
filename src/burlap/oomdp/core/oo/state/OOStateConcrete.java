package burlap.oomdp.core.oo.state;

import burlap.oomdp.core.state.MutableState;
import burlap.oomdp.core.state.State;

import java.util.*;

/**
 * @author James MacGlashan.
 */
public class OOStateConcrete implements MutableOOState{


	protected Map<String, List<ObjectInstance>> objectsByClass = new HashMap<String, List<ObjectInstance>>();
	protected Map<String, ObjectInstance> objectsMap = new HashMap<String, ObjectInstance>();

	public OOStateConcrete() {
	}

	public OOStateConcrete(OOState srcOOState){
		for(ObjectInstance o : srcOOState.objects()){
			this.addObject((ObjectInstance)o.copy());
		}
	}

	@Override
	public List<Object> variableKeys() {
		List<Object> keys = new ArrayList<Object>();
		for(ObjectInstance ob : this.objects()){
			for(Object varKey : ob.variableKeys()){
				OOVariableKey ookey = new OOVariableKey(ob.name(), varKey);
				keys.add(ookey);
			}
		}
		return keys;
	}

	@Override
	public Object get(Object variableKey) {

		OOVariableKey key = this.constructKey(variableKey);

		ObjectInstance ob = this.objectsMap.get(key.obName);
		if(ob != null) {
			return ob.get(key.obVarKey);
		}

		throw new RuntimeException("Cannot return value for key " + key.toString() + " because there is no object with the specified name.");
	}

	@Override
	public MutableState set(Object variableKey, Object value) {

		OOVariableKey key = this.constructKey(variableKey);
		ObjectInstance ob = this.objectsMap.get(key);
		if(ob == null){
			throw new RuntimeException("Cannot set value for key " + key.toString() + " because there is no object with the specified name.");
		}
		ObjectInstance touchedOb = (ObjectInstance)ob.copy();
		if(!(touchedOb instanceof MutableState)){
			throw new RuntimeException("Cannot set value for object " + touchedOb.name() + " because it does not implement MutableState");
		}
		((MutableState)ob).set(key.obVarKey, value);
		this.addObject(touchedOb);

		return this;
	}

	@Override
	public State copy() {
		return new OOStateConcrete(this);
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
			ObjectInstance copied = (ObjectInstance)stored.copy();
			copied.copyWithName(newName);
			this.addObject(copied);

		}
		return this;
	}

	@Override
	public int numTotalObjects() {
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
		return this.objectsByClass.get(oclass);
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		for(ObjectInstance o : this.objects()){
			buf.append(o.toString());
		}
		return buf.toString();
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

	protected List<ObjectInstance> getOrGenerateObjectClassList(String className){
		List<ObjectInstance> obs = this.objectsByClass.get(className);
		if(obs == null){
			obs = new ArrayList<ObjectInstance>();
			this.objectsByClass.put(className, obs);
		}
		return obs;
	}

	protected OOVariableKey constructKey(Object variableKey){

		if(variableKey instanceof OOVariableKey) {

			OOVariableKey key = (OOVariableKey) variableKey;
			return key;

		}
		else if(variableKey instanceof String){
			OOVariableKey key = new OOVariableKey((String)variableKey);
			return key;
		}

		throw new RuntimeException("Cannot construct OOVariable key from object that is already a OOVariableKey, or a String.");
	}
}
