package burlap.oomdp.core.states;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.objects.ImmutableObjectInstance;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;


/**
 * An immutable state cannot be changed (nor subclassed). It is particularly useful when memory management is crucial, as
 * copies are only created when actual changes are applied to an ImmutableState (copy on write). Performance will not be 
 * affected dramatically if they use a StateBuilder object to keep track of desired state changes.
 * @author James MacGlashan
 *
 */
public final class ImmutableState extends OOMDPState implements State {

	
	/**
	 * List of observable object instances that define the state
	 */
	private final List <ImmutableObjectInstance>							objectInstances;
	
	/**
	 * List of hidden object instances that facilitate domain dynamics and infer observable values
	 */
	private final List <ImmutableObjectInstance>							hiddenObjectInstances;
	
	/**
	 * Map from object names to their instances
	 */
	private final Map <String, Integer>							objectMap;
	
	
	/**
	 * Map of object instances organized by class name
	 */
	private final List<List <Integer>>							objectIndexByTrueClass;

	private final Map<String, Integer>							objectClassMap;
	
	private final int											numObservableObjects;
	private final int											numHiddenObjects;
	//private final Domain domain;
	
	
	public ImmutableState(){
		this.objectInstances = Collections.unmodifiableList(new ArrayList<ImmutableObjectInstance>());
		this.hiddenObjectInstances = Collections.unmodifiableList(new ArrayList<ImmutableObjectInstance>());
		this.objectIndexByTrueClass = Collections.unmodifiableList(new ArrayList<List<Integer>>());
		this.objectClassMap = Collections.unmodifiableMap(new HashMap<String, Integer>());
		this.objectMap = Collections.unmodifiableMap(new HashMap<String, Integer>());
		this.numObservableObjects = 0;
		this.numHiddenObjects = 0;
		//this.domain = domain;
	}
	
	
	/**
	 * Constructs an immutable copy from any state object. All underlying lists are also copied so changes
	 * to the original state are not reflected in this copy.
	 * @param s the source state from which this state will be initialized.
	 */
	public ImmutableState(State s){
		int size = 2 * (s.numTotalObjects());
		HashMap<String, Integer> objectMap = new HashMap<String, Integer>(size);
		Map<String, Integer> objectClassMap = new HashMap<String, Integer>();
		
		List<ImmutableObjectInstance> immutableObjects = null;
		if(s instanceof ImmutableState){
			immutableObjects = this.createImmutableObjects(((ImmutableState)s).getObservableObjects());
		}
		else{
			immutableObjects = this.createImmutableObjects(s.getAllObjects());
		}
		List<ImmutableObjectInstance> immutableHidden = new ArrayList<ImmutableObjectInstance>();
		if(s instanceof ImmutableState) {
			immutableObjects = this.createImmutableObjects(((ImmutableState)s).getHiddenObjects());
		}
		List<ImmutableObjectInstance> objectInstances = this.createObjectLists(immutableObjects, objectMap, 0);
		List<ImmutableObjectInstance> hiddenObjectsInstances = this.createObjectLists(immutableHidden, objectMap, objectInstances.size());
		
		this.objectInstances = Collections.unmodifiableList(objectInstances);
		this.hiddenObjectInstances = Collections.unmodifiableList(hiddenObjectsInstances);
		this.numObservableObjects = this.objectInstances.size();
		this.numHiddenObjects = this.hiddenObjectInstances.size();
		
		int numberClasses = objectClassMap.size();
		List<List<Integer>> objectIndexByTrueClass = new ArrayList<List<Integer>>(numberClasses);
		size /= 2;
		for (int i = 0 ; i < numberClasses; i++) {
			objectIndexByTrueClass.add(new ArrayList<Integer>(size));
		}
		
		this.addObjectListToList(this.objectInstances, this.numObservableObjects, numberClasses, objectIndexByTrueClass, objectClassMap);
		this.addObjectListToList(this.hiddenObjectInstances, this.numHiddenObjects, numberClasses, objectIndexByTrueClass, objectClassMap);
		
		this.objectIndexByTrueClass = Collections.unmodifiableList(objectIndexByTrueClass);
		this.objectClassMap = Collections.unmodifiableMap(objectClassMap);
		this.objectMap = Collections.unmodifiableMap(objectMap);
	}
	
	public ImmutableState(List<ObjectInstance> objects) {
		Map<String, Integer> objectMap = new HashMap<String, Integer>(2 * objects.size());
		Map<String, Integer> objectClassMap = new HashMap<String, Integer>();
		
		List<ImmutableObjectInstance> immutableObjects = this.createImmutableObjects(objects);
		List<ImmutableObjectInstance> objectInstances = this.createObjectLists(immutableObjects, objectMap, 0);
		
		this.objectInstances = Collections.unmodifiableList(objectInstances);
		
		this.hiddenObjectInstances = Collections.unmodifiableList(new ArrayList<ImmutableObjectInstance>());
		this.numObservableObjects = this.objectInstances.size();
		this.numHiddenObjects = 0;
		
		List<List<Integer>> objectIndexByTrueClass = 
				this.buildObjectIndexByTrueClass(objectInstances, hiddenObjectInstances, objectClassMap);
		this.objectClassMap = Collections.unmodifiableMap(objectClassMap);
		this.objectIndexByTrueClass = Collections.unmodifiableList(objectIndexByTrueClass);
		
		this.objectMap = Collections.unmodifiableMap(objectMap);
		
	}
	
	public ImmutableState(List<ImmutableObjectInstance> objects, List<ImmutableObjectInstance> hiddenObjects, Map<String, Integer> objectClassMap) {
		int size = 2 * (objects.size() + hiddenObjects.size());
		HashMap<String, Integer> objectMap = new HashMap<String, Integer>(size);
		objectClassMap = new HashMap<String, Integer>(objectClassMap);
		List<ImmutableObjectInstance> objectInstances = this.createObjectLists(objects, objectMap, 0);
		
		List<ImmutableObjectInstance> hiddenObjectsInstances = this.createObjectLists(hiddenObjects, objectMap, objectInstances.size());
		this.objectInstances = Collections.unmodifiableList(objectInstances);
		
		this.hiddenObjectInstances = Collections.unmodifiableList(hiddenObjectsInstances);
		this.numObservableObjects = this.objectInstances.size();
		this.numHiddenObjects = hiddenObjects.size();
		
		int numberClasses = objectClassMap.size();
		List<List<Integer>> objectIndexByTrueClass = new ArrayList<List<Integer>>(numberClasses);
		size = objects.size() + hiddenObjects.size();
		for (int i = 0 ; i < numberClasses; i++) {
			objectIndexByTrueClass.add(new ArrayList<Integer>(size));
		}
		
		this.addObjectListToList(this.objectInstances, this.numObservableObjects, numberClasses, objectIndexByTrueClass, objectClassMap);
		this.addObjectListToList(this.hiddenObjectInstances, this.numHiddenObjects, numberClasses, objectIndexByTrueClass, objectClassMap);
		this.objectIndexByTrueClass = Collections.unmodifiableList(objectIndexByTrueClass);
		this.objectClassMap = Collections.unmodifiableMap(objectClassMap);
		this.objectMap = Collections.unmodifiableMap(objectMap);
		
	}
	
	public ImmutableState(List<ImmutableObjectInstance> objects, List<ImmutableObjectInstance> hiddenObjects, Map<String, Integer> objectClassMap, List<List<Integer>> objectIndexByTrueClass, Map<String, Integer> objectMap) {
		this.objectInstances = Collections.unmodifiableList(objects);
		this.hiddenObjectInstances = Collections.unmodifiableList(hiddenObjects);
		this.numObservableObjects = this.objectInstances.size();
		this.numHiddenObjects = this.hiddenObjectInstances.size();
		this.objectIndexByTrueClass = Collections.unmodifiableList(objectIndexByTrueClass);
		this.objectClassMap = Collections.unmodifiableMap(objectClassMap);
		this.objectMap = Collections.unmodifiableMap(objectMap);	
	}
	
	/**
	 * This method doesn't actually copy. This implementation only copies on write.
	 * @return the same state.
	 */
	public ImmutableState copy(){
		return this;
	}

	
	private final List<ImmutableObjectInstance> createImmutableObjects(List<ObjectInstance> objects) {
		List<ImmutableObjectInstance> immutableObjects = new ArrayList<ImmutableObjectInstance>(objects.size());
		for (ObjectInstance object : objects) {
			ImmutableObjectInstance immutable = 
					(object instanceof ImmutableObjectInstance) ? 
							(ImmutableObjectInstance)object : new ImmutableObjectInstance(object);
			immutableObjects.add(immutable);
		}
		return immutableObjects;
	}
	
	private final List<ImmutableObjectInstance> createObjectLists(List<ImmutableObjectInstance> objectList, Map<String, Integer> objectMap, int offset) {
		List<ImmutableObjectInstance> objectInstances = new ArrayList<ImmutableObjectInstance>(objectList.size());
		for (ImmutableObjectInstance object : objectList) {
			if (object == null) {
				continue;
			}
			
			Integer displaced = objectMap.put(object.getName(), objectInstances.size() + offset);
			if (displaced != null) {
				objectMap.put(object.getName(), displaced);
			} else {
				objectInstances.add(object);
			}
		}
		return objectInstances;
	}
	
	private final List<List<Integer>> buildObjectIndexByTrueClass(List<ImmutableObjectInstance> objects, List<ImmutableObjectInstance> hiddenObjects, Map<String, Integer> objectClassMap) {
		int size = Math.max(10, objectClassMap.size());
		
		List<List<Integer>> objectIndexByTrueClass = new ArrayList<List<Integer>>(size);
		this.addObjectListToList(objects, objects.size(), 0, objectIndexByTrueClass, objectClassMap);
		this.addObjectListToList(hiddenObjects, hiddenObjects.size(), objectIndexByTrueClass.size(), objectIndexByTrueClass, objectClassMap);
		
		/*
		Map<String, List<Integer>> immutableListObjectsMap = new HashMap<String, List<Integer>>();
		for (Map.Entry<String, List<Integer>> entry : objectIndexByTrueClass.entrySet()) {
			immutableListObjectsMap.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
		}*/
		return objectIndexByTrueClass;
	}

	public Map<String, Integer> getObjectMap() {
		return this.objectMap;
	}

	private void addObjectListToList(List<ImmutableObjectInstance> objects, int size, int numClasses,
			List<List<Integer>> objectIndexByTrueClass, Map<String, Integer> objectClassMap) {
		
		for (int i = 0; i < size; i++) {
			ObjectInstance object = objects.get(i);
			String objectClassName = object.getTrueClassName();
			Integer position = objectClassMap.get(objectClassName);
			
			if (position == null) {
				position = numClasses++;
				objectClassMap.put(objectClassName, position );
				objectIndexByTrueClass.add(new ArrayList<Integer>(size));
			}
			List<Integer> objectsOfClass = 
					objectIndexByTrueClass.get(position);
			objectsOfClass.add(i);
		}
	}
	
	@Override
	public final ImmutableState addObject(ObjectInstance object) {
		return this.addAllObjects(Arrays.asList(object));
	}
	
	@Override
	public final ImmutableState addAllObjects(Collection<ObjectInstance> objectsToAdd) {
		List<ImmutableObjectInstance> objects = new ArrayList<ImmutableObjectInstance>(this.objectInstances);
		for (ObjectInstance object: objectsToAdd) {
			objects.add(new ImmutableObjectInstance(object));
		}
		return new ImmutableState(objects, this.hiddenObjectInstances, this.objectClassMap);
	}
	
	public final ImmutableState makeObjectsHidden(Collection<ObjectInstance> objectsToHide) {
		List<ImmutableObjectInstance> objects = new ArrayList<ImmutableObjectInstance>(this.objectInstances);
		objects.removeAll(objectsToHide);
		List<ImmutableObjectInstance> hiddenObjects = new ArrayList<ImmutableObjectInstance>(this.hiddenObjectInstances);
		
		for (ObjectInstance object : objectsToHide) {
			if (!(object instanceof ImmutableObjectInstance)) {
				throw new RuntimeException("This object instance should have been immutable");
			}
			hiddenObjects.add((ImmutableObjectInstance)object);
		}
		
		return new ImmutableState(objects, hiddenObjects, this.objectClassMap);
	}
	
	public final ImmutableState makeObjectsObservable(Collection<ObjectInstance> objectsToObserve) {
		List<ImmutableObjectInstance> hiddenObjects = new ArrayList<ImmutableObjectInstance>(this.hiddenObjectInstances);
		hiddenObjects.removeAll(objectsToObserve);
		List<ImmutableObjectInstance> observableObjects = new ArrayList<ImmutableObjectInstance>(this.objectInstances);
		
		for (ObjectInstance object : objectsToObserve) {
			if (!(object instanceof ImmutableObjectInstance)) {
				throw new RuntimeException("This object instance should have been immutable");
			}
			observableObjects.add((ImmutableObjectInstance)object);
		}
		
		return new ImmutableState(observableObjects, hiddenObjects, this.objectClassMap);
	}
	
	public final ImmutableState removeObject(String objectName) {
		Integer index = this.objectMap.get(objectName);
		if (index == null) {
			return this;
		}
		
		List<ImmutableObjectInstance> objects = this.objectInstances;
		List<ImmutableObjectInstance> hiddenObjects = this.hiddenObjectInstances;
		
		if (index < this.numObservableObjects) {
			objects = new ArrayList<ImmutableObjectInstance>(objects);
			objects.remove((int)index);
		}
		else
		{
			hiddenObjects = new ArrayList<ImmutableObjectInstance>(hiddenObjects);
			hiddenObjects.remove((int)index - this.numObservableObjects);
		}

		return new ImmutableState(objects, hiddenObjects, this.objectClassMap);
	}
	
	public final ImmutableState removeObject(ObjectInstance object) {
		return this.removeObject(object.getName());
	}
	
	public final ImmutableState removeAllObjects(Collection<ObjectInstance> objectsToRemove) {
		List<Integer> indices = new ArrayList<Integer>();
		for (ObjectInstance object : objectsToRemove) {
			Integer index = this.objectMap.get(object.getName());
			if (index != null)
			indices.add(index);
		}
		
		List<ImmutableObjectInstance> objects = new ArrayList<ImmutableObjectInstance>(this.objectInstances);
		List<ImmutableObjectInstance> hiddenObjects = new ArrayList<ImmutableObjectInstance>(this.hiddenObjectInstances);
		
		Collections.sort(indices, Collections.reverseOrder());
		for (Integer i : indices) {
			if (i < this.numObservableObjects) {
				objects.remove(i);
			}
			else {
				objects.remove(i - this.numObservableObjects);
			}
		}
		
		return new ImmutableState(objects, hiddenObjects, this.objectClassMap);	
	}
	
	public final ImmutableState replaceObject(ObjectInstance objectToReplace, ObjectInstance newObject) {
		String oldObjectName = objectToReplace.getName();
		String newObjectName = newObject.getName();
		if (!oldObjectName.equals(newObjectName) || !objectToReplace.getTrueClassName().equals(newObject.getTrueClassName())) {
			throw new RuntimeException("In order to replace, the objects must have the same name and class. Try remove and add instead");
		}
		if (!(newObject instanceof ImmutableObjectInstance)) {
			throw new RuntimeException("Object " + newObject.getName() + " must be of type ImmutableObjectInstance");
		}
		
		Integer index = this.objectMap.get(oldObjectName);
		if (index == null) {
			return this;
		}
		List<ImmutableObjectInstance> objects = this.objectInstances;
		List<ImmutableObjectInstance> hiddenObjects = this.hiddenObjectInstances;
		
		if (index < this.numObservableObjects) {
			objects = new ArrayList<ImmutableObjectInstance>(objects);
			objects.set(index, (ImmutableObjectInstance)newObject);
		} else {
			hiddenObjects = new ArrayList<ImmutableObjectInstance>(objects);
			hiddenObjects.set(index - this.numObservableObjects, (ImmutableObjectInstance)newObject);
		}
		
		
		return new ImmutableState(objects, hiddenObjects, this.objectClassMap, this.objectIndexByTrueClass, this.objectMap);
	}
	
	public final ImmutableState replaceAllObjects(List<ObjectInstance> objectsToRemove, List<ObjectInstance> objectsToAdd) {
		List<ImmutableObjectInstance> objects = new ArrayList<ImmutableObjectInstance>(this.objectInstances);
		List<ImmutableObjectInstance> hiddenObjects = new ArrayList<ImmutableObjectInstance>(this.hiddenObjectInstances);
		
		if (objectsToRemove.size() != objectsToAdd.size()) {
			throw new RuntimeException("This method requires the two collections to agree in size");
		}
		
		for (int i = 0; i < objectsToRemove.size(); i++) {
			ObjectInstance objectToRemove = objectsToRemove.get(i);
			ObjectInstance objectToAdd = objectsToAdd.get(i);
			
			String oldObjectName = objectToRemove.getName();
			String newObjectName = objectToAdd.getName();
			if (!oldObjectName.equals(newObjectName) || !objectToRemove.getTrueClassName().equals(objectToAdd.getTrueClassName())) {
				throw new RuntimeException("The objects must have matching names and classes");
			}
			
			Integer index = this.objectMap.get(objectToRemove.getName());
			if (index == null) {
				throw new RuntimeException("Object " + oldObjectName + " does not exist");
			}
			if (!(objectToAdd instanceof ImmutableObjectInstance)) {
				throw new RuntimeException("Object " + objectToAdd.getName() + " must be of type ImmutableObjectInstance");
			}
			if (index < this.numObservableObjects) {
				objects.set(index, (ImmutableObjectInstance)objectToAdd);
			}
			else {
				hiddenObjects.set(index - this.numObservableObjects, (ImmutableObjectInstance)objectToAdd);
			}
		}

		return new ImmutableState(objects, hiddenObjects, this.objectClassMap, this.objectIndexByTrueClass, this.objectMap);
	}
	
	
	/**
	 * Renames the identifier for object instance o in this state to newName.
	 * @param o the object instance to rename in this state
	 * @param newName the new name of the object instance
	 */
	@Override
	public State renameObject(ObjectInstance o, String newName){
		ObjectInstance newObject = o.setName(newName);
		return this.replaceObject(o, newObject);
	}
	
	
	/**
	 * This method computes a matching from objects in the receiver to value-identical objects in the parameter state so. The matching
	 * is returned as a map from the object names in the receiving state to the matched objects in state so. If
	 * enforceStateExactness is set to true, then the returned matching will be an empty map if the two states
	 * are not OO-MDP-wise identical (i.e., if there is a not a bijection
	 *  between value-identical objects of the two states). If enforceExactness is false and the states are not identical,
	 *  the the method will return the largest matching between objects that can be made.
	 * @param so the state to whose objects the receiving state's objects should be matched
	 * @param enforceStateExactness whether to require that states are identical to return a matching
	 * @return a matching from this receiving state's objects to objects in so that have identical values. 
	 */
	
	public Map <String, String> getObjectMatchingTo(State so, boolean enforceStateExactness){
		Map <String, String> matching = new HashMap<String, String>();
		
		if(this.numTotalObjects() != so.numTotalObjects() && enforceStateExactness){
			return new HashMap<String, String>(); //states are not equal and therefore cannot be matched
		}
		
		Set<String> matchedObs = new HashSet<String>();
		
		for(Map.Entry<String, Integer> entry : this.objectClassMap.entrySet()){
			String oclass = entry.getKey();
			List <Integer> objectIndices = this.objectIndexByTrueClass.get(entry.getValue());
			List <ObjectInstance> oobjects = so.getObjectsOfClass(oclass);
			if(objectIndices.size() != so.numTotalObjects() && enforceStateExactness){
				return new HashMap<String, String>(); //states are not equal and therefore cannot be matched
			}
			
			for(Integer i : objectIndices){
				ObjectInstance o = this.getObject(i);
				boolean foundMatch = false;
				for(ObjectInstance oo : oobjects){
					if(matchedObs.contains(oo.getName())){
						continue; //already matched this one; check another
					}
					if(o.valueEquals(oo)){
						foundMatch = true;
						matchedObs.add(oo.getName());
						matching.put(o.getName(), oo.getName());
						break;
					}
				}
				if(!foundMatch && enforceStateExactness){
					return new HashMap<String, String>(); //states are not equal and therefore cannot be matched
				}
			}
			
		}
		
		return matching;
	}
	
	
	
	
	@Override
	public boolean equals(Object other){
	
		if(this == other){
			return true;
		}
		
		if(!(other instanceof ImmutableState)){
			return false;
		}
		
		ImmutableState so = (ImmutableState)other;
		
		if(this.numTotalObjects() != so.numTotalObjects()){
			return false;
		}
		
		
		Set<Integer> matchedObjects = new HashSet<Integer>((int)(this.numTotalObjects() / 0.75) + 1);
		for (int i = 0; i < this.objectIndexByTrueClass.size(); i++){
			List<Integer> objectIndices = this.objectIndexByTrueClass.get(i);
			if (objectIndices.isEmpty()) {
				continue;
			}
			
			String oclass = this.getObject(objectIndices.get(0)).getObjectClass().name;
			String ooclass = "";
			List <Integer> oobjectsIndices = so.objectIndexByTrueClass.get(i);
			if (!oobjectsIndices.isEmpty()) {
				ooclass = so.getObject(objectIndices.get(0)).getObjectClass().name;
			}
			if (oclass.equals(ooclass)) {
				int position = so.objectClassMap.get(oclass);
				oobjectsIndices = so.objectIndexByTrueClass.get(position);
			} 
			 
			if(objectIndices.size() != oobjectsIndices.size()){
				return false;
			}
			
			for(Integer j : objectIndices){
				ObjectInstance o = this.getObject(j);
				ObjectInstance oo = so.getObject(j);
				// first check if the object in the same order is the same
				if (o.valueEquals(oo)) {
					continue;
				}
				// otherwise get the otherone by name
				other = so.getObject(o.getName());
				if (o.valueEquals(oo)){
					continue;
				}
				
				// otherwise if there is another object with a different name but is equals
				boolean foundMatch = false;
				for(Integer k : oobjectsIndices){
					if(matchedObjects.contains(k)){
						continue;
					}
					
					if(o.valueEquals(so.getObject(k))){
						foundMatch = true;
						matchedObjects.add(k);
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
	 * Returns the number of observable and hidden object instances in this state.
	 * @return the number of observable and hidden object instances in this state.
	 */
	public int numTotalObjects(){
		return this.numObservableObjects + this.numHiddenObjects;
	}
	
	/**
	 * Returns the number of observable object instances in this state.
	 * @return the number of observable object instances in this state.
	 */
	public int numObservableObjects(){
		return this.numObservableObjects;
	}
	
	/**
	 * Returns the number of hidden object instances in this state.
	 * @return the number of hideen object instances in this state.
	 */
	public int numHiddenObjects(){
		return this.numHiddenObjects;
	}
	
	public ObjectInstance getObject(Integer i) {
		if (i == null) {
			return null;
		}
		int item = i;
		if (item < 0) {
			return null;
		}
		if (item < this.numObservableObjects) {
			return this.objectInstances.get(i);
		}
		item -= this.numObservableObjects;
		if (i < this.numHiddenObjects) {
			return this.hiddenObjectInstances.get(i);
		}
		return null;
	}
	
	/**
	 * Returns the object in this state with the name oname
	 * @param oname the name of the object instance to return
	 * @return the object instance with the name oname or null if there is no object in this state named oname
	 */
	public ObjectInstance getObject(String oname){
		Integer pos = this.objectMap.get(oname);
		return (pos == null) ? null : this.getObject(pos);
	}
	
	/**
	 * Returns the observable object instance indexed at position i
	 * @param i the index of the observable object instance to return
	 * @return the observable object instance indexed at position i, or null if i > this.numObservableObjects()
	 */
	public ObjectInstance getObservableObjectAt(int i){
		if(i > this.numObservableObjects){
			return null;
		}
		return objectInstances.get(i);
	}
	
	
	/**
	 * Returns the hidden object instance indexed at position i
	 * @param i the index of the hidden object instance to return
	 * @return the hidden object instance indexed at position i, or null if i > this.numHiddenObjects()
	 */
	public ObjectInstance getHiddenObjectAt(int i){
		if(i > this.numHiddenObjects){
			return null;
		}
		return hiddenObjectInstances.get(i);
	}
	
	
	/**
	 * Returns the list of observable object instances in this state.
	 * @return the list of observable object instances in this state.
	 */
	public List <ObjectInstance> getObservableObjects(){
		return new ArrayList<ObjectInstance>(this.objectInstances);
	}
	
	
	/**
	 * Returns the list of hidden object instances in this state.
	 * @return the list of hidden object instances in this state.
	 */
	public List <ObjectInstance> getHiddenObjects(){
		return new ArrayList<ObjectInstance>(this.hiddenObjectInstances);
	}
	
	
	/**
	 * Returns the list of observable and hidden object instances in this state.
	 * @return the list of observable and hidden object instances in this state.
	 */
	public List <ObjectInstance> getAllObjects(){
		List <ObjectInstance> objects = new ArrayList <ObjectInstance>(objectInstances.size() + hiddenObjectInstances.size());
		objects.addAll(this.objectInstances);
		objects.addAll(this.hiddenObjectInstances);
		return objects;
	}
	
	/**
	 * Returns all objects that belong to the object class named oclass
	 * @param oclass the name of the object class for which objects should be returned
	 * @return all objects that belong to the object class named oclass
	 */
	public List <ObjectInstance> getObjectsOfClass(String oclass){
		
		Integer position = this.objectClassMap.get(oclass);
		if(position == null){
			return Lists.newArrayList();
		}
		List <Integer> tmp = objectIndexByTrueClass.get(position);
		List<ObjectInstance> objects = new ArrayList<ObjectInstance>(tmp.size());
		
		for (Integer i : tmp) {
			objects.add(this.getObject(i));
		}
		return objects;
	}
	
	
	/**
	 * Returns the first indexed object of the object class named oclass
	 * @param oclass the name of the object class for which the first indexed object should be returned.
	 * @return the first indexed object of the object class named oclass
	 */
	public ObjectInstance getFirstObjectOfClass(String oclass){
		Integer position = this.objectClassMap.get(oclass);
		List <Integer> obs = this.objectIndexByTrueClass.get(position);
		if(obs != null && obs.size() > 0){
			return this.getObject(obs.get(0));
		}
		return null;
	}
	
	/**
	 * Returns a set of of the object class names for all object classes that have instantiated objects in this state.
	 * @return a set of of the object class names for all object classes that have instantiated objects in this state.
	 */
	public Set <String> getObjectClassesPresent(){
		return objectClassMap.keySet();
	}
	
	
	/**
	 * Returns a list of list of object instances, grouped by object class
	 * @return a list of list of object instances, grouped by object class
	 */
	public List <List <ObjectInstance>> getAllObjectsByClass(){
		List<List<ObjectInstance>> allObjects = new ArrayList<List<ObjectInstance>>(this.objectIndexByTrueClass.size());
		for (List<Integer> indices : this.objectIndexByTrueClass) {
			List<ObjectInstance> objects = new ArrayList<ObjectInstance>(indices.size());
			for (Integer i : indices) {
				objects.add(this.getObject(i));
			}
		}
		return allObjects;
	}
	
	public List<List<Integer>> getAllObjectIndicesByTrueClass() {
		return this.objectIndexByTrueClass;
	}
	
	public Map<String, Integer> getObjectClassMap() {
		return this.objectClassMap;
	}
	
	
	/**
	 * Returns a string representation of this state using only observable object instances.
	 * @return a string representation of this state using only observable object instances.
	 */
	public String getStateDescription(){
		
		StringBuilder builder = new StringBuilder(200);
		String desc = "";
		for(ObjectInstance o : objectInstances){
			builder = o.buildObjectDescription(builder).append("\n");
		}
		
		return builder.toString();
	}
	
	
	/**
	 * Returns a string representation of this state using observable and hidden object instances.
	 * @return a string representation of this state using observable and hidden object instances.
	 */
	public String getCompleteStateDescription(){
		
		StringBuilder builder = new StringBuilder(200);
		for(ObjectInstance o : objectInstances){
			builder = o.buildObjectDescription(builder).append("\n");
		}
		for(ObjectInstance o : hiddenObjectInstances){
			builder = o.buildObjectDescription(builder).append("\n");
		}
		
		
		return builder.toString();
		
	}
	
	/**
	 * Given an array of parameter object classes and an array of their corresponding parameter order groups,
	 * returns all possible object instance bindings to the parameters, excluding bindings that are equivalent due
	 * to the parameter order grouping.
	 * @param paramClasses the name of object classes to which the bound object instances must belong
	 * @param paramOrderGroups the parameter order group names.
	 * @return A list of all possible object instance bindings for the parameters, were a binding is represented by a list of object instance names
	 */
	
	public List <List <ObjectInstance>> getPossibleObjectBindingsGivenParamOrderGroups(String [] paramClasses, String [] paramOrderGroups){
		List<List<Integer>> resIndices = this.getPossibleBindingsIndicesGivenParamOrderGroups(paramClasses, paramOrderGroups);
		return this.getBindingObjectsFromIndices(resIndices);
		
	}

	public List <List <String>> getPossibleBindingsGivenParamOrderGroups(String [] paramClasses, String [] paramOrderGroups){
		List<List<Integer>> resIndices = this.getPossibleBindingsIndicesGivenParamOrderGroups(paramClasses, paramOrderGroups);
		return this.getBindingsFromIndices(resIndices);
	}
	
	private List<List<Integer>> getPossibleBindingsIndicesGivenParamOrderGroups(String [] paramClasses, String [] paramOrderGroups) {
		List <List <Integer>> currentBindingSets = new ArrayList <List<Integer>>();
		List<Integer> paramClassIds = this.getClassIds(paramClasses);
		List <String> uniqueRenames = this.identifyUniqueClassesInParameters(Arrays.asList(paramOrderGroups));
		List <Integer> uniqueParamClasses = this.identifyUniqueClassesInParameters(paramClassIds);
		Map <String, Integer> uniqueParamClassCounts = this.getUniqueClassCounts(uniqueRenames, Arrays.asList(paramOrderGroups));
		
		List<Integer> currentObjects = new ArrayList<Integer>();
		int initialSize = 1;
		//first make sure we have objects for each class parameter; if not return empty list
		for(Integer classId : uniqueParamClasses){
			int n = this.getNumOccurencesOfClassInParameters(classId, paramClassIds);
			
			List <Integer> objectsOfClass = objectIndexByTrueClass.get(classId);
			int numObjects = objectsOfClass.size();
			if(numObjects < n){
				return new ArrayList <List <Integer>>();
			}
			initialSize *= numObjects;
			currentObjects.addAll(objectsOfClass);
		}
		List <List <Integer>> resIndices = new ArrayList <List<Integer>>(initialSize);
		final Set<Integer> combSet = new HashSet<Integer>(2 * this.numTotalObjects());
		Predicate<Integer> retainPredicate = new Predicate<Integer>() {
			@Override
			public boolean apply(Integer arg0) {
				return !combSet.contains(arg0);
			}
		};
		this.getPossibleRenameBindingsHelper(resIndices, currentBindingSets, 0, currentObjects, uniqueRenames, paramClassIds, 
				Arrays.asList(paramOrderGroups), uniqueParamClassCounts,  combSet, retainPredicate);
		return resIndices;
		
	}
	
	private List<Integer> getClassIds(String[] classes) {
		List<Integer> ids = new ArrayList<Integer>(classes.length);
		for (String c : classes) {
			ids.add(this.objectClassMap.get(c));
		}
		return ids;
	}
	
	private Map<String, Integer> getUniqueClassCounts(Collection<String> uniqueParamClasses, List<String> paramClassIds) {
		Map<String, Integer> counts = new HashMap<String, Integer>(uniqueParamClasses.size());
		for (String id : uniqueParamClasses) {
			counts.put(id, Collections.frequency(paramClassIds, id));
		}
		return counts;
	}
	
	private List <List <String>> getBindingsFromIndices(List<List<Integer>> allIndices) {
		List<List<String>> res = new ArrayList<List<String>>(allIndices.size());
		for (List<Integer> indices : allIndices) {
			List<String> objects = new ArrayList<String>(indices.size());
			for (Integer i : indices) {
				objects.add(this.getObject(i).getName());
			}
			res.add(objects);
		}
		return res;
	}
	
	private List <List <ObjectInstance>> getBindingObjectsFromIndices(List<List<Integer>> allIndices) {
		List<List<ObjectInstance>> res = new ArrayList<List<ObjectInstance>>(allIndices.size());
		for (List<Integer> indices : allIndices) {
			List<ObjectInstance> objects = new ArrayList<ObjectInstance>(indices.size());
			for (Integer i : indices) {
				objects.add(this.getObject(i));
			}
			res.add(objects);
		}
		return res;
	}
	
	
	
	private void getPossibleRenameBindingsHelper(List <List <Integer>> res, List <List <Integer>> currentBindingSets, int bindIndex,
			Iterable<Integer> remainingObjects, List <String> uniqueOrderGroups, List<Integer> paramClassIds, List<String> paramOrderGroups, 
			Map<String, Integer> uniqueParamOrderCounts, Set<Integer> combSet, Predicate<Integer> retainPredicate){
		
		if(bindIndex == uniqueOrderGroups.size()){
			//base case, put it all together and add it to the result
			res.add(this.getBindingFromCombinationSet(currentBindingSets, uniqueOrderGroups, paramOrderGroups));
			return ;
		}
		
		//otherwise we're in the recursive case
		
		String r = uniqueOrderGroups.get(bindIndex);
		Integer cId = this.parameterClassAssociatedWithOrderGroup(r, paramOrderGroups, paramClassIds);
		List <Integer> cands = this.objectsMatchingClass(remainingObjects, cId);
		int k = uniqueParamOrderCounts.get(r);
		
		//int k = this.numOccurencesOfOrderGroup(r, paramOrderGroupIds);
		
		int n = cands.size();
		int [] comb = this.initialComb(k, n);
		
		
		List<Integer> combList = this.getObjectsFromComb(cands, comb);
		combSet.addAll(combList);
		this.addBindingCombination(res, currentBindingSets, bindIndex,
				remainingObjects, uniqueOrderGroups, paramClassIds,
				paramOrderGroups, uniqueParamOrderCounts, combList, retainPredicate, combSet);
		
		
		while(nextComb(comb, k, n) == 1){
			combList = this.getObjectsFromComb(cands, comb);
			this.addBindingCombination(res, currentBindingSets, bindIndex,
					remainingObjects, uniqueOrderGroups, paramClassIds,
					paramOrderGroups, uniqueParamOrderCounts, combList, retainPredicate, combSet);
		}
	}


	private void addBindingCombination(List<List<Integer>> res,
			List<List<Integer>> currentBindingSets, int bindIndex,
			Iterable<Integer> remainingObjects, List<String> uniqueOrderGroups,
			List<Integer> paramClassIds, List<String> paramOrderGroups, Map<String, Integer> uniqueParamClassCounts, List<Integer> cb, Predicate<Integer> retainPredicate, Set<Integer> combSet) {
		
		List <List<Integer>> nextBinding = new ArrayList<List<Integer>>(currentBindingSets.size() + 1);
		nextBinding.addAll(currentBindingSets);
		nextBinding.add(cb);
		
		combSet.clear();
		combSet.addAll(cb);
		
		Iterable<Integer> nextObsReamining = this.objectListDifference(remainingObjects, retainPredicate);
		
		//recursive step
		this.getPossibleRenameBindingsHelper(res, nextBinding, bindIndex+1, nextObsReamining, uniqueOrderGroups, paramClassIds, 
				paramOrderGroups, uniqueParamClassCounts, combSet, retainPredicate);
	}
	
	// Reorders objects, to make list removal fast
	private Iterable<Integer> objectListDifference(Iterable<Integer> objects, Predicate<Integer> retainPredicate){
		return Iterables.filter(objects, retainPredicate);
	}
	
	private int getNumOccurencesOfClassInParameters(Integer id, List<Integer> ids ){
		return Collections.frequency(ids, id);
	}
	/*
	private int getNumOccurencesOfClassInParameters(String className, String [] paramClasses){
		int num = 0;
		for(int i = 0; i < paramClasses.length; i++){
			if(paramClasses[i].equals(className)){
				num++;
			}
		}
		return num;
	}*/
	
	private <T> List <T> identifyUniqueClassesInParameters(List<T> paramClassIds){
		Set <T> unique = new TreeSet <T>(paramClassIds);
		/*for(T id : paramClassIds){
			if(!unique.contains(id)){
				unique.add(id);
			}
		}*/
		
		return new ArrayList<T>(unique);
	}
	
	
	
	private int numOccurencesOfOrderGroup(String rename, String [] orderGroups){
		int num = 0;
		for(int i = 0; i < orderGroups.length; i++){
			if(orderGroups[i].equals(rename)){
				num++;
			}
		}
		
		return num;
		
	}
	
	private Integer parameterClassAssociatedWithOrderGroup(String orderGroup, List<String> orderGroups, List<Integer> paramClasses){
		for (int i = 0; i < orderGroups.size(); i++) {
			if (orderGroups.get(i).equals(orderGroup)) {
				return paramClasses.get(i);
			}
		}
		return -1;
	}
	
	private List<Integer> objectsMatchingClass(Iterable <Integer> sourceObs, Integer id){
		List <Integer> res = Lists.newArrayList(sourceObs);
		List<Integer> allClassObjects = this.objectIndexByTrueClass.get(id);
		res.retainAll(allClassObjects);
		
		return res;
	}
	
	/*
	private List <Integer> objectsMatchingClass(Collection <Integer> sourceObs, String cname){
		List <Integer> res = new ArrayList<Integer>(sourceObs);
		int pos = this.objectClassMap.get(cname);
		List<Integer> allClassObjects = this.objectIndexByTrueClass.get(pos);
		res.retainAll(allClassObjects);
		
		return res;
	}*/
	
	
	
	/**
	 * for a specific parameter order group, return a possible binding
	 * @param comboSets is a list of the bindings for each order group. For instance, if the order groups for each parameter were P, Q, P, Q, R; then there would be three lists
	 * @param orderGroupAssociatedWithSet which order group each list of bindings in comboSets is for
	 * @param orderGroups the parameter order groups for each parameter
	 * @return a binding as a list of object instance names
	 */
	private List <Integer> getBindingFromCombinationSet(List <List <Integer>> comboSets, List <String> orderGroupAssociatedWithSet, List<String> orderGroups){
		
		List<Integer> res = new ArrayList<Integer>(orderGroups.size());
		//apply the parameter bindings for each rename combination
		for(int i = 0; i < comboSets.size(); i++){
			List <Integer> renameCombo = comboSets.get(i);
			String r = orderGroupAssociatedWithSet.get(i);
			
			//find the parameter indices that match this rename and set a binding accordingly
			int ind = 0;
			for(int j = 0; j < orderGroups.size(); j++){
				if (orderGroups.get(j).equals(r)) {
					res.add(renameCombo.get(ind));
					ind++;
				}
			}
		}
		
		return res;
	}
	
	/*
	 private List <String> getBindngFromCombinationSet(List <List <String>> comboSets, List <String> orderGroupAssociatedWithSet, String [] orderGroups){
		
		List <String> res = new ArrayList <String>(orderGroups.length);
		//add the necessary space first
		for(int i = 0; i < orderGroups.length; i++){
			res.add("");
		}
		
		//apply the parameter bindings for each rename combination
		for(int i = 0; i < comboSets.size(); i++){
			List <String> renameCombo = comboSets.get(i);
			String r = orderGroupAssociatedWithSet.get(i);
			
			//find the parameter indices that match this rename and set a binding accordingly
			int ind = 0;
			for(int j = 0; j < orderGroups.length; j++){
				if(orderGroups[j].equals(r)){
					res.set(j, renameCombo.get(ind));
					ind++;
				}
			}
		}
		
		return res;
	}
	 */
	
	
	private List <List <Integer>> getAllCombinationsOfObjects(List <Integer> objects, int k){
		
		List <List<Integer>> allCombs = new ArrayList <List<Integer>>();
		
		int n = this.numObservableObjects;
		int [] comb = this.initialComb(k, n);
		List<Integer> initialComb = this.getObjectsFromComb(objects, comb);
		allCombs.add(initialComb);
		while(nextComb(comb, k, n) == 1){
			allCombs.add(this.getObjectsFromComb(objects, comb));
		}
		
		return allCombs;
		
	}
	
	private List<Integer> getObjectsFromComb(List<Integer> allObjects, int[] comb) {
		List<Integer> objects = new ArrayList<Integer>(comb.length);
		for (int i : comb){ 
			objects.add(allObjects.get(i));
		}
		return objects;
	}
	
	
	@Deprecated
	private List <Integer> getListOfBindingsFromCombination(List <Integer> objects, int [] comb){
		List <Integer> res = new ArrayList <Integer>(comb.length);
		for(int i = 0; i < comb.length; i++){
			//res.add(objects.get(comb[i]).getName());
		}
		return res;
	}
	
	
	private int [] initialComb(int k, int n){
		int [] res = new int[k];
		for(int i = 0; i < k; i++){
			res[i] = i;
		}
		
		return res;
	}
	
	
	/**
	 * Iterates through combinations. 
	 * Modified code from: http://compprog.wordpress.com/tag/generating-combinations/
	 * @param comb the last combination of elements selected
	 * @param k number of elements in any combination (n choose k)
	 * @param n number of possible elements (n choose k)
	 * @return 0 when there are no more combinations; 1 when a new combination is generated
	 */
	private int nextComb(int [] comb, int k, int n){
		
		int i = k-1;
		comb[i]++;
		
		while(i > 0 && comb[i] >= n-k+1+i){
			i--;
			comb[i]++;
		}
		
		if(comb[0] > n-k){
			return 0;
		}
		
		/* comb now looks like (..., x, n, n, n, ..., n).
		Turn it into (..., x, x + 1, x + 2, ...) */
		for(i = i+1; i < k; i++){
			comb[i] = comb[i-1] + 1;
		}
		
		return 1;
	}


	@Override
	public Map<String, List<String>> getAllUnsetAttributes() {
		Map<String, List<String>> unset = new HashMap<String, List<String>>();
		for(ObjectInstance o : this.objectInstances){
			List<String> unsetA = o.unsetAttributes();
			if(unsetA.size() > 0){
				unset.put(o.getName(), unsetA);
			}
		}
		for(ObjectInstance o : this.hiddenObjectInstances){
			List<String> unsetA = o.unsetAttributes();
			if(unsetA.size() > 0){
				unset.put(o.getName(), unsetA);
			}
		}

		return unset;
	}


	@Override
	public String getCompleteStateDescriptionWithUnsetAttributesAsNull() {
		String desc = "";
		for(ObjectInstance o : objectInstances){
			desc = desc + o.getObjectDesriptionWithNullForUnsetAttributes() + "\n";
		}
		for(ObjectInstance o : hiddenObjectInstances){
			desc = desc + o.getObjectDesriptionWithNullForUnsetAttributes() + "\n";
		}


		return desc;
	}
}
