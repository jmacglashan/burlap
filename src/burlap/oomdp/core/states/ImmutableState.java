package burlap.oomdp.core.states;

import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import burlap.oomdp.core.objects.ImmutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;


/**
 * An immutable state cannot be changed (nor subclassed). It is particularly useful when memory management is crucial, as
 * copies are only created when actual changes are applied to an ImmutableState (copy on write). If many objects are altered
 * during a grounded action update, it is recommended to use the bulk modification methods (replaceAllObjects, addAllObjects, etc).
 * @author Stephen Brawner, James MacGlashan
 *
 */
public final class ImmutableState extends OOMDPState implements ImmutableStateInterface {

	
	/**
	 * List of observable object instances that define the state
	 */
	private final ImmutableList <ImmutableObjectInstance>			objectInstances;
	
	/**
	 * Map from object names to their instances
	 */
	private final TObjectIntHashMap <String>						objectMap;
	
	/**
	 * Map of object instances organized by class name
	 */
	private final ImmutableList<TIntArrayList>						objectIndexByTrueClass;

	private final TObjectIntHashMap<String>							objectClassMap;
	
	private final int												numObservableObjects;
	private final int												hashCode;
	
	
	public ImmutableState(){
		this.objectInstances = ImmutableList.copyOf(new ArrayList<ImmutableObjectInstance>());
		this.objectIndexByTrueClass = ImmutableList.copyOf(new ArrayList<TIntArrayList>());
		this.objectClassMap = new TObjectIntHashMap<String>();
		this.objectMap = new TObjectIntHashMap<String>();
		this.numObservableObjects = 0;
		this.hashCode = 0;
	}
	
	
	/**
	 * Constructs an immutable copy from any state object. All underlying lists are also copied so changes
	 * to the original state are not reflected in this copy.
	 * @param s the source state from which this state will be initialized.
	 */
	public ImmutableState(State s){
		int size = 2 * (s.numTotalObjects());
		TObjectIntHashMap<String> objectMap = new TObjectIntHashMap<String>(size, 0.5f, -1);
		TObjectIntHashMap<String> objectClassMap = new TObjectIntHashMap<String>(s.getObjectClassesPresent().size()*2, 0.5f, -1);
		
		List<ImmutableObjectInstance> immutableObjects = null;
		immutableObjects = this.createImmutableObjects(s.getAllObjects());
		List<ImmutableObjectInstance> objectInstances = this.createObjectLists(immutableObjects, objectMap, 0);
		
		this.objectInstances = ImmutableList.copyOf(objectInstances);
		this.numObservableObjects = this.objectInstances.size();
		
		int numberClasses = objectClassMap.size();
		List<TIntArrayList> objectIndexByTrueClass = new ArrayList<TIntArrayList>(numberClasses);
		size /= 2;
		for (int i = 0 ; i < numberClasses; i++) {
			objectIndexByTrueClass.add(new TIntArrayList(size));
		}
		
		this.addObjectListToList(this.objectInstances, this.numObservableObjects, numberClasses, objectIndexByTrueClass, objectClassMap);
		
		this.objectIndexByTrueClass = ImmutableList.copyOf(objectIndexByTrueClass);
		this.objectClassMap = objectClassMap;
		this.objectMap = objectMap;
		this.hashCode = 0;
	}
	
	public ImmutableState(List<ImmutableObjectInstance> objects, TObjectIntHashMap<String> objectClassMap) {
		int size = 2 * (objects.size());
		TObjectIntHashMap<String> objectMap = new TObjectIntHashMap<String>(size, 0.5f, -1);
		objectClassMap = new TObjectIntHashMap<String>(objectClassMap);
		List<ImmutableObjectInstance> objectInstances = this.createObjectLists(objects, objectMap, 0);
		
		this.objectInstances = ImmutableList.copyOf(objectInstances);
		
		this.numObservableObjects = this.objectInstances.size();
		
		int numberClasses = objectClassMap.size();
		List<TIntArrayList> objectIndexByTrueClass = new ArrayList<TIntArrayList>(numberClasses);
		size = objects.size();
		for (int i = 0 ; i < numberClasses; i++) {
			objectIndexByTrueClass.add(new TIntArrayList(size));
		}
		
		this.addObjectListToList(this.objectInstances, this.numObservableObjects, numberClasses, objectIndexByTrueClass, objectClassMap);
		this.objectIndexByTrueClass = ImmutableList.copyOf(objectIndexByTrueClass);//Collections.unmodifiableList();
		this.objectClassMap = objectClassMap;//Collections.unmodifiableMap();
		this.objectMap = objectMap;//Collections.unmodifiableMap();
		this.hashCode = 0;
	}
	
	public ImmutableState(ImmutableList<ImmutableObjectInstance> objects, TObjectIntHashMap<String> objectClassMap, ImmutableList<TIntArrayList> objectIndexByTrueClass, TObjectIntHashMap <String> objectMap, int hashCode) {
		this.objectInstances = objects;
		this.numObservableObjects = this.objectInstances.size();
		this.objectIndexByTrueClass = objectIndexByTrueClass;
		this.objectClassMap = objectClassMap;
		this.objectMap = objectMap;
		this.hashCode = hashCode;
	}


	/**
	 * This method doesn't actually copy. This implementation only copies on write.
	 * @return the same state.
	 */
	public ImmutableState copy(){
		return this;
	}

	public final ImmutableState replaceAndHash(ImmutableList<ImmutableObjectInstance> objects, int code) {
		return new ImmutableState(objects, this.objectClassMap, this.objectIndexByTrueClass, this.objectMap, code);
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
	
	private final List<ImmutableObjectInstance> createObjectLists(List<ImmutableObjectInstance> objectList, TObjectIntHashMap<String> objectMap, int offset) {
		List<ImmutableObjectInstance> objectInstances = new ArrayList<ImmutableObjectInstance>(objectList.size());
		for (ImmutableObjectInstance object : objectList) {
			if (object == null) {
				continue;
			}
			
			int displaced = objectMap.put(object.getName(), objectInstances.size() + offset);
			if (displaced >= 0) {
				objectMap.put(object.getName(), displaced);
			} else {
				objectInstances.add(object);
			}
		}
		return objectInstances;
	}

	private void addObjectListToList(List<ImmutableObjectInstance> objects, int size, int numClasses,
			List<TIntArrayList> objectIndexByTrueClass, TObjectIntHashMap<String> objectClassMap) {
		
		for (int i = 0; i < size; i++) {
			ObjectInstance object = objects.get(i);
			String objectClassName = object.getClassName();
			int position = objectClassMap.get(objectClassName);
			
			if (position < 0) {
				position = numClasses++;
				objectClassMap.put(objectClassName, position );
				objectIndexByTrueClass.add(new TIntArrayList(size));
			}
			TIntArrayList objectsOfClass = 
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
		return new ImmutableState(objects, this.objectClassMap);
	}
	
	public final ImmutableState removeObject(String objectName) {
		int index = this.objectMap.get(objectName);
		if (index < 0) {
			return this;
		}
		
		List<ImmutableObjectInstance> objects = 
				new ArrayList<ImmutableObjectInstance>(this.objectInstances);
		objects.remove((int)index);
		return new ImmutableState(objects, this.objectClassMap);
	}
	
	public final ImmutableState removeObject(ObjectInstance object) {
		return this.removeObject(object.getName());
	}
	
	public final ImmutableState removeAllObjects(Collection<ObjectInstance> objectsToRemove) {
		List<Integer> indices = new ArrayList<Integer>();
		for (ObjectInstance object : objectsToRemove) {
			Integer index = this.objectMap.get(object.getName());
			if (index >= 0)
			indices.add(index);
		}
		
		List<ImmutableObjectInstance> objects = new ArrayList<ImmutableObjectInstance>(this.objectInstances);
		
		Collections.sort(indices, Collections.reverseOrder());
		for (Integer i : indices) {
			objects.remove(i);
		}
		
		return new ImmutableState(objects, this.objectClassMap);	
	}
	
	public final ImmutableState replaceObject(ObjectInstance objectToReplace, ObjectInstance newObject) {
		String oldObjectName = objectToReplace.getName();
		String newObjectName = newObject.getName();
		if (!oldObjectName.equals(newObjectName) || !objectToReplace.getClassName().equals(newObject.getClassName())) {
			throw new RuntimeException("In order to replace, the objects must have the same name and class. Try remove and add instead");
		}
		if (!(newObject instanceof ImmutableObjectInstance)) {
			throw new RuntimeException("Object " + newObject.getName() + " must be of type ImmutableObjectInstance");
		}
		
		int index = this.objectMap.get(oldObjectName);
		if (index < 0) {
			return this;
		}
		List<ImmutableObjectInstance> objects = 
				 new ArrayList<ImmutableObjectInstance>(this.objectInstances);
		objects.set(index, (ImmutableObjectInstance)newObject);

		return new ImmutableState(ImmutableList.copyOf(objects), this.objectClassMap, this.objectIndexByTrueClass, this.objectMap, 0);
	}
	
	public final ImmutableState replaceAllObjects(List<ImmutableObjectInstance> objectsToRemove, List<ImmutableObjectInstance> objectsToAdd) {
		List<ImmutableObjectInstance> objects = new ArrayList<ImmutableObjectInstance>(this.objectInstances);
		
		if (objectsToRemove.size() != objectsToAdd.size()) {
			throw new RuntimeException("This method requires the two collections to agree in size");
		}
		
		for (int i = 0; i < objectsToRemove.size(); i++) {
			ObjectInstance objectToRemove = objectsToRemove.get(i);
			ObjectInstance objectToAdd = objectsToAdd.get(i);
			
			String oldObjectName = objectToRemove.getName();
			String newObjectName = objectToAdd.getName();
			if (!oldObjectName.equals(newObjectName) || !objectToRemove.getClassName().equals(objectToAdd.getClassName())) {
				throw new RuntimeException("The objects must have matching names and classes");
			}
			
			int index = this.objectMap.get(objectToRemove.getName());
			if (index < 0) {
				throw new RuntimeException("Object " + oldObjectName + " does not exist");
			}
			if (!(objectToAdd instanceof ImmutableObjectInstance)) {
				throw new RuntimeException("Object " + objectToAdd.getName() + " must be of type ImmutableObjectInstance");
			}
			if (index < this.numObservableObjects) {
				objects.set(index, (ImmutableObjectInstance)objectToAdd);
			}
		}

		return new ImmutableState(ImmutableList.copyOf(objects), this.objectClassMap, this.objectIndexByTrueClass, this.objectMap, 0);
	}
	
	public final ImmutableState replaceAllObjectsUnsafe(List<ObjectInstance> objectsToAdd) {
		List<ImmutableObjectInstance> objects = new ArrayList<ImmutableObjectInstance>(objectsToAdd.size());
		for (ObjectInstance obj : objectsToAdd) {
			objects.add((ImmutableObjectInstance)obj);
		}
		return new ImmutableState(ImmutableList.copyOf(objects), this.objectClassMap, this.objectIndexByTrueClass, this.objectMap, 0);
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
	
	@Override
	public <T> State setObjectsValue(String objectName, String attName, T value) {
		int index = this.objectMap.get(objectName);
		if (index < 0) {
			throw new RuntimeException("Object " + objectName + " does not exist in this state");
		}
		ObjectInstance obj = this.objectInstances.get(index);
		return this.replaceObject(obj, obj.setValue(attName, value));
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
		for (TObjectIntIterator<String> it = this.objectClassMap.iterator(); it.hasNext();it.advance()){
			String oclass = it.key();
			int classId = it.value();
		//for(Map.Entry<String, Integer> entry : this.objectClassMap.entrySet()){
			//String oclass = entry.getKey();
			TIntArrayList objectIndices = this.objectIndexByTrueClass.get(classId);
			List <ObjectInstance> oobjects = so.getObjectsOfClass(oclass);
			if(objectIndices.size() != so.numTotalObjects() && enforceStateExactness){
				return new HashMap<String, String>(); //states are not equal and therefore cannot be matched
			}
			
			for (int i = 0; i < objectIndices.size(); i++) {
				int objIndex = objectIndices.get(i);
				//for(Integer i : objectIndices.){
				ObjectInstance o = this.objectInstances.get(objIndex);
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
			TIntArrayList objectIndices = this.objectIndexByTrueClass.get(i);
			if (objectIndices.isEmpty()) {
				continue;
			}
			
			String oclass = this.getObject(objectIndices.get(0)).getObjectClass().name;
			String ooclass = "";
			TIntArrayList oobjectsIndices = so.objectIndexByTrueClass.get(i);
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
			for (int j = 0; j < objectIndices.size(); j++) {
				int objIndex = objectIndices.get(j);
				//for(Integer j : objectIndices){
				ObjectInstance o = this.getObject(objIndex);
				ObjectInstance oo = so.getObject(objIndex);
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
				for (int k = 0; k < oobjectsIndices.size(); k++) {
					int oobjIndex = oobjectsIndices.get(k);
				//for(Integer k : oobjectsIndices){
					if(matchedObjects.contains(oobjIndex)){
						continue;
					}
					
					if(o.valueEquals(so.getObject(oobjIndex))){
						foundMatch = true;
						matchedObjects.add(oobjIndex);
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
		return this.numObservableObjects;
	}
	
	/**
	 * Returns the number of observable object instances in this state.
	 * @return the number of observable object instances in this state.
	 */
	public int numObservableObjects(){
		return this.numObservableObjects;
	}
		
	public ObjectInstance getObject(int i) {
		return objectInstances.get(i);
	}
	
	/**
	 * Returns the object in this state with the name oname
	 * @param oname the name of the object instance to return
	 * @return the object instance with the name oname or null if there is no object in this state named oname
	 */
	public ObjectInstance getObject(String oname){
		int pos = this.objectMap.get(oname);
		return (pos < 0) ? null : this.objectInstances.get(pos);
	}
	
	
	/**
	 * Returns the list of observable object instances in this state.
	 * @return the list of observable object instances in this state.
	 */
	public List <ObjectInstance> getObservableObjects(){
		return new ArrayList<ObjectInstance>(this.objectInstances);
	}
	
	/**
	 * Returns the list of observable and hidden object instances in this state.
	 * @return the list of observable and hidden object instances in this state.
	 */
	public List <ObjectInstance> getAllObjects(){
		return new ArrayList<ObjectInstance>(this.objectInstances);
	}
	
	public ImmutableList<ImmutableObjectInstance> getImmutableObjects() {
		return this.objectInstances;
	}
	
	/**
	 * Returns all objects that belong to the object class named oclass
	 * @param oclass the name of the object class for which objects should be returned
	 * @return all objects that belong to the object class named oclass
	 */
	public List <ObjectInstance> getObjectsOfClass(String oclass){
		
		int position = this.objectClassMap.get(oclass);
		if(position < 0){
			return Lists.newArrayList();
		}
		TIntArrayList tmp = objectIndexByTrueClass.get(position);
		List<ObjectInstance> objects = new ArrayList<ObjectInstance>(tmp.size());
		for (int i = 0; i < tmp.size(); i++) {
		//for (Integer i : tmp) {
			objects.add(this.objectInstances.get(tmp.get(i)));
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
		TIntArrayList obs = this.objectIndexByTrueClass.get(position);
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
		for (TIntArrayList indices : this.objectIndexByTrueClass) {
			List<ObjectInstance> objects = new ArrayList<ObjectInstance>(indices.size());
			for (int i = 0; i < indices.size(); i++) {
			//for (Integer i : indices) {
				objects.add(this.objectInstances.get(indices.get(i)));
			}
		}
		return allObjects;
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
		List<TIntArrayList> resIndices = this.getPossibleBindingsIndicesGivenParamOrderGroups(paramClasses, paramOrderGroups);
		return this.getBindingObjectsFromIndices(resIndices);
		
	}

	public List <List <String>> getPossibleBindingsGivenParamOrderGroups(String [] paramClasses, String [] paramOrderGroups){
		List<TIntArrayList> resIndices = this.getPossibleBindingsIndicesGivenParamOrderGroups(paramClasses, paramOrderGroups);
		return this.getBindingsFromIndices(resIndices);
	}
	
	private List<TIntArrayList> getPossibleBindingsIndicesGivenParamOrderGroups(String [] paramClasses, String [] paramOrderGroups) {
		List <TIntArrayList> currentBindingSets = new ArrayList <TIntArrayList>();
		TIntArrayList paramClassIds = this.getClassIds(paramClasses);
		List <String> uniqueRenames = this.identifyUniqueClassesInParameters(Arrays.asList(paramOrderGroups));
		TIntArrayList uniqueParamClasses = new TIntArrayList(new TIntHashSet(paramClassIds));//this.identifyUniqueClassesInParameters(paramClassIds);
		Map <String, Integer> uniqueParamClassCounts = this.getUniqueClassCounts(uniqueRenames, Arrays.asList(paramOrderGroups));
		
		List<Integer> currentObjects = new ArrayList<Integer>();
		int initialSize = 1;
		//first make sure we have objects for each class parameter; if not return empty list
		for (int i = 0; i < uniqueParamClasses.size(); i++) {
			int classId = uniqueParamClasses.get(i);
		//for(Integer classId : uniqueParamClasses){
			int n = this.getNumOccurencesOfClassInParameters(classId, paramClassIds);
			
			TIntArrayList objectsOfClass = objectIndexByTrueClass.get(classId);
			int numObjects = objectsOfClass.size();
			if(numObjects < n){
				return new ArrayList <TIntArrayList>();
			}
			initialSize *= numObjects;
			for (int j = 0; j < objectsOfClass.size(); j++) {
				currentObjects.add(objectsOfClass.get(j));
			}
		}
		List <TIntArrayList> resIndices = new ArrayList <TIntArrayList>(initialSize);
		final TIntHashSet combSet = new TIntHashSet(2 * this.numTotalObjects());
		Predicate<Integer> retainPredicate = new Predicate<Integer>() {
			@Override
			public boolean apply(Integer arg0) {
				return !combSet.contains(arg0);
			}
		};
		this.getPossibleRenameBindingsHelper(resIndices, currentBindingSets, 0, currentObjects, uniqueRenames, paramClassIds, 
				Arrays.asList(paramOrderGroups), uniqueParamClassCounts, retainPredicate, combSet);
		return resIndices;
		
	}
	
	private TIntArrayList getClassIds(String[] classes) {
		TIntArrayList ids = new TIntArrayList(classes.length);
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
	
	private List <List <String>> getBindingsFromIndices(List<TIntArrayList> allIndices) {
		List<List<String>> res = new ArrayList<List<String>>(allIndices.size());
		for (TIntArrayList indices : allIndices) {
			List<String> objects = new ArrayList<String>(indices.size());
			for (int i = 0; i < indices.size(); i++){
			//for (Integer i : indices) {
				objects.add(this.objectInstances.get(indices.get(i)).getName());
			}
			res.add(objects);
		}
		return res;
	}
	
	private List <List <ObjectInstance>> getBindingObjectsFromIndices(List<TIntArrayList> allIndices) {
		List<List<ObjectInstance>> res = new ArrayList<List<ObjectInstance>>(allIndices.size());
		for (TIntArrayList indices : allIndices) {
			List<ObjectInstance> objects = new ArrayList<ObjectInstance>(indices.size());
			for (int i = 0; i < indices.size(); i++){
				//for (Integer i : indices) {
					objects.add(this.objectInstances.get(indices.get(i)));
			}
			res.add(objects);
		}
		return res;
	}
	
	
	
	private void getPossibleRenameBindingsHelper(List <TIntArrayList> res, List <TIntArrayList> currentBindingSets, int bindIndex,
			Iterable<Integer> remainingObjects, List <String> uniqueOrderGroups, TIntArrayList paramClassIds, List<String> paramOrderGroups, 
			Map<String, Integer> uniqueParamOrderCounts, Predicate<Integer> retainPredicate, TIntHashSet combSet){
		
		if(bindIndex == uniqueOrderGroups.size()){
			//base case, put it all together and add it to the result
			res.add(this.getBindingFromCombinationSet(currentBindingSets, uniqueOrderGroups, paramOrderGroups));
			return ;
		}
		
		//otherwise we're in the recursive case
		
		String r = uniqueOrderGroups.get(bindIndex);
		Integer cId = this.parameterClassAssociatedWithOrderGroup(r, paramOrderGroups, paramClassIds);
		TIntArrayList cands = this.objectsMatchingClass(remainingObjects, cId);
		int k = uniqueParamOrderCounts.get(r);
		
		//int k = this.numOccurencesOfOrderGroup(r, paramOrderGroupIds);
		
		int n = cands.size();
		int [] comb = this.initialComb(k, n);
		
		
		TIntArrayList combList = this.getObjectsFromComb(cands, comb);
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


	private void addBindingCombination(List<TIntArrayList> res,
			List<TIntArrayList> currentBindingSets, int bindIndex,
			Iterable<Integer> remainingObjects, List<String> uniqueOrderGroups,
			TIntArrayList paramClassIds, List<String> paramOrderGroups, Map<String, Integer> uniqueParamClassCounts, 
			TIntArrayList cb, Predicate<Integer> retainPredicate, TIntHashSet combSet) {
		
		List <TIntArrayList> nextBinding = new ArrayList<TIntArrayList>(currentBindingSets.size() + 1);
		nextBinding.addAll(currentBindingSets);
		nextBinding.add(cb);
		
		combSet.clear();
		combSet.addAll(cb);
		
		Iterable<Integer> nextObsReamining = this.objectListDifference(remainingObjects, retainPredicate);
		//recursive step
		this.getPossibleRenameBindingsHelper(res, nextBinding, bindIndex+1, nextObsReamining, uniqueOrderGroups, paramClassIds, 
				paramOrderGroups, uniqueParamClassCounts, retainPredicate, combSet);
	}
	
	// Reorders objects, to make list removal fast
	private Iterable<Integer> objectListDifference(Iterable<Integer> objects, Predicate<Integer> retainPredicate){
		return Iterables.filter(objects, retainPredicate);
	}
	
	private int getNumOccurencesOfClassInParameters(int id, TIntArrayList ids ){
		int count = 0;
		for (int i = 0; i < ids.size(); i++) {
			count += (ids.get(i) == id) ? 1 : 0;
		}
		return count;
	}
	
	private <T> List <T> identifyUniqueClassesInParameters(List<T> paramClassIds){
		Set <T> unique = new TreeSet <T>(paramClassIds);
		return new ArrayList<T>(unique);
	}
	
	private Integer parameterClassAssociatedWithOrderGroup(String orderGroup, List<String> orderGroups, TIntArrayList paramClasses){
		for (int i = 0; i < orderGroups.size(); i++) {
			if (orderGroups.get(i).equals(orderGroup)) {
				return paramClasses.get(i);
			}
		}
		return -1;
	}
	
	private TIntArrayList objectsMatchingClass(Iterable<Integer> sourceObs, Integer id){
		TIntArrayList res = new TIntArrayList();
		for (Integer obj : sourceObs) {
			res.add(obj);
		}
		TIntArrayList allClassObjects = this.objectIndexByTrueClass.get(id);
		res.retainAll(allClassObjects);
		
		return res;
	}
	
	/**
	 * for a specific parameter order group, return a possible binding
	 * @param comboSets is a list of the bindings for each order group. For instance, if the order groups for each parameter were P, Q, P, Q, R; then there would be three lists
	 * @param orderGroupAssociatedWithSet which order group each list of bindings in comboSets is for
	 * @param orderGroups the parameter order groups for each parameter
	 * @return a binding as a list of object instance names
	 */
	private TIntArrayList getBindingFromCombinationSet(List <TIntArrayList> comboSets, List <String> orderGroupAssociatedWithSet, List<String> orderGroups){
		
		TIntArrayList res = new TIntArrayList(orderGroups.size());
		//apply the parameter bindings for each rename combination
		for(int i = 0; i < comboSets.size(); i++){
			TIntArrayList renameCombo = comboSets.get(i);
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
	
	private TIntArrayList getObjectsFromComb(TIntArrayList allObjects, int[] comb) {
		TIntArrayList objects = new TIntArrayList(comb.length);
		for (int i : comb){ 
			objects.add(allObjects.get(i));
		}
		return objects;
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
		return unset;
	}


	@Override
	public String getCompleteStateDescriptionWithUnsetAttributesAsNull() {
		String desc = "";
		for(ObjectInstance o : objectInstances){
			desc = desc + o.getObjectDescriptionWithNullForUnsetAttributes() + "\n";
		}
		return desc;
	}
	
	public boolean isHashed() {
		return this.hashCode != 0;
	}
	
	@Override
	public int hashCode() {
		if (this.hashCode == 0) {
			return super.hashCode();
		}
		return this.hashCode;
	}


	@Override
	public Iterator<ImmutableObjectInstance> iterator() {
		return this.objectInstances.iterator();
	}
}
