package burlap.oomdp.core;

import java.util.*;

import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;


/**
 * State objects are a collection of Object Instances.
 * @author James MacGlashan
 *
 */
public class State {

	
	/**
	 * List of observable object instances that define the state
	 */
	protected List <ObjectInstance>							objectInstances;
	
	/**
	 * List of hidden object instances that facilitate domain dynamics and infer observable values
	 */
	protected List <ObjectInstance>							hiddenObjectInstances;
	
	/**
	 * Map from object names to their instances
	 */
	protected Map <String, ObjectInstance>					objectMap;
	
	
	/**
	 * Map of object instances organized by class name
	 */
	protected Map <String, List <ObjectInstance>>			objectIndexByTrueClass;

	
	
	public State(){
		this.initDataStructures();
	}
	
	
	/**
	 * Initializes this state as a deep copy of the object instances in the provided source state s
	 * @param s the source state from which this state will be initialized.
	 */
	public State(State s){
		
		
		this.initDataStructures();
		
		for(ObjectInstance o : s.objectInstances){
			this.addObject(o.copy());
		}
		
		for(ObjectInstance o : s.hiddenObjectInstances){
			this.addObject(o.copy());
		}
		
	}
	
	/**
	 * Returns a deep copy of this state.
	 * @return a deep copy of this state.
	 */
	public State copy(){
		return new State(this);
	}
	
	
	/**
	 * Performs a semi-deep copy of the state in which only the objects with the names in deepCopyObjectNames are deep copied and the rest of the
	 * objects are shallowed copied.
	 * @param deepCopyObjectNames the names of the objects to be deep copied.
	 * @return a new state that is a mix of a shallow and deep copy of this state.
	 */
	public State semiDeepCopy(String...deepCopyObjectNames){
		Set<ObjectInstance> deepCopyObjectSet = new HashSet<ObjectInstance>(deepCopyObjectNames.length);
		for(String n : deepCopyObjectNames){
			deepCopyObjectSet.add(this.getObject(n));
		}
		return this.semiDeepCopy(deepCopyObjectSet);
	}
	
	
	/**
	 * Performs a semi-deep copy of the state in which only the objects in deepCopyObjects are deep copied and the rest of the
	 * objects are shallowed copied.
	 * @param deepCopyObjects the objects to be deep copied
	 * @return a new state that is a mix of a shallow and deep copy of this state.
	 */
	public State semiDeepCopy(ObjectInstance...deepCopyObjects){
		
		Set<ObjectInstance> deepCopyObjectSet = new HashSet<ObjectInstance>(deepCopyObjects.length);
		for(ObjectInstance d : deepCopyObjects){
			deepCopyObjectSet.add(d);
		}
		
		return this.semiDeepCopy(deepCopyObjectSet);
	}
	
	
	/**
	 * Performs a semi-deep copy of the state in which only the objects in deepCopyObjects are deep copied and the rest of the
	 * objects are shallowed copied.
	 * @param deepCopyObjects the objects to be deep copied
	 * @return a new state that is a mix of a shallow and deep copy of this state.
	 */
	public State semiDeepCopy(Set<ObjectInstance> deepCopyObjects){
		
		State s = new State();
		for(ObjectInstance o : this.objectInstances){
			if(deepCopyObjects.contains(o)){
				s.addObject(o.copy());
			}
			else{
				s.addObject(o);
			}
		}
		
		for(ObjectInstance o : this.hiddenObjectInstances){
			if(deepCopyObjects.contains(o)){
				s.addObject(o.copy());
			}
			else{
				s.addObject(o);
			}
		}
		
		return s;
	}
	
	
	protected void initDataStructures(){
		
		objectInstances = new ArrayList <ObjectInstance>();
		hiddenObjectInstances = new ArrayList <ObjectInstance>();
		objectMap = new HashMap <String, ObjectInstance>();
		
		objectIndexByTrueClass = new HashMap <String, List <ObjectInstance>>();
	}
	
	
	/**
	 * Adds object instance o to this state.
	 * @param o the object instance to be added to this state.
	 */
	public void addObject(ObjectInstance o){
		
		String oname = o.getName();
		
		if(objectMap.containsKey(oname)){
			return ; //don't add an object that conflicts with another object of the same name
		}
		
		
		objectMap.put(oname, o);
		
		
		if(o.getObjectClass().hidden){
			hiddenObjectInstances.add(o);
		}
		else{
			objectInstances.add(o);
		}
		
		
		this.addObjectClassIndexing(o);
		
		
	}
	
	private void addObjectClassIndexing(ObjectInstance o){
		
		String otclass = o.getTrueClassName();
		
		//manage true indexing
		if(objectIndexByTrueClass.containsKey(otclass)){
			objectIndexByTrueClass.get(otclass).add(o);
		}
		else{
			
			ArrayList <ObjectInstance> classList = new ArrayList <ObjectInstance>();
			classList.add(o);
			objectIndexByTrueClass.put(otclass, classList);
			
		}
		
	}
	
	
	/**
	 * Removes the object instance with the name oname from this state.
	 * @param oname the name of the object instance to remove.
	 */
	public void removeObject(String oname){
		this.removeObject(objectMap.get(oname));
	}
	
	
	/**
	 * Removes the object instance o from this state.
	 * @param o the object instance to remove from this state.
	 */
	public void removeObject(ObjectInstance o){
		if(o == null){
			return ;
		}
		
		String oname = o.getName();
		
		if(!objectMap.containsKey(oname)){
			return ; //make sure we're removing something that actually exists in this state!
		}
		
		if(o.getObjectClass().hidden){
			hiddenObjectInstances.remove(o);
		}
		else{
			objectInstances.remove(o);
		}
		
		objectMap.remove(oname);
		
		this.removeObjectClassIndexing(o);
		
	}
	
	
	
	private void removeObjectClassIndexing(ObjectInstance o){
		
		
		String otclass = o.getTrueClassName();
		List <ObjectInstance> classTList = objectIndexByTrueClass.get(otclass);
		
		//if this index has more than one entry, then we can just remove from it and be done
		if(classTList.size() > 1){
			classTList.remove(o);
		}
		else{
			//otherwise we have to remove class entries for it
			objectIndexByTrueClass.remove(otclass);
		}
		
		
		
	}
	
	
	/**
	 * Renames the identifier for the object instance currently named originalName with the name newName.
	 * @param originalName the original name of the object instance to be renamed in this state
	 * @param newName the new name of the object instance
	 */
	public void renameObject(String originalName, String newName){
		ObjectInstance o = objectMap.get(originalName);
		o.setName(newName);
		objectMap.remove(originalName);
		objectMap.put(newName, o);
	}
	
	
	/**
	 * Renames the identifier for object instance o in this state to newName.
	 * @param o the object instance to rename in this state
	 * @param newName the new name of the object instance
	 */
	public void renameObject(ObjectInstance o, String newName){
		String originalName = o.getName();
		o.setName(newName);
		objectMap.remove(originalName);
		objectMap.put(newName, o);
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
		
		if(this.numTotalObjets() != so.numTotalObjets() && enforceStateExactness){
			return new HashMap<String, String>(); //states are not equal and therefore cannot be matched
		}
		
		Set<String> matchedObs = new HashSet<String>();
		
		for(List <ObjectInstance> objects : objectIndexByTrueClass.values()){
			
			String oclass = objects.get(0).getTrueClassName();
			List <ObjectInstance> oobjects = so.getObjectsOfTrueClass(oclass);
			if(objects.size() != oobjects.size() && enforceStateExactness){
				return new HashMap<String, String>(); //states are not equal and therefore cannot be matched
			}
			
			for(ObjectInstance o : objects){
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
		
		if(!(other instanceof State)){
			return false;
		}
		
		State so = (State)other;
		
		if(this.numTotalObjets() != so.numTotalObjets()){
			return false;
		}
		
		Set<String> matchedObjects = new HashSet<String>();
		for(List <ObjectInstance> objects : objectIndexByTrueClass.values()){
			
			String oclass = objects.get(0).getTrueClassName();
			List <ObjectInstance> oobjects = so.getObjectsOfTrueClass(oclass);
			if(objects.size() != oobjects.size()){
				return false;
			}
			
			for(ObjectInstance o : objects){
				boolean foundMatch = false;
				for(ObjectInstance oo : oobjects){
					String ooname = oo.getName();
					if(matchedObjects.contains(ooname)){
						continue;
					}
					if(o.valueEquals(oo)){
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
	 * Returns the number of observable and hidden object instances in this state.
	 * @return the number of observable and hidden object instances in this state.
	 */
	public int numTotalObjets(){
		return objectInstances.size() + hiddenObjectInstances.size();
	}
	
	/**
	 * Returns the number of observable object instances in this state.
	 * @return the number of observable object instances in this state.
	 */
	public int numObservableObjects(){
		return objectInstances.size();
	}
	
	/**
	 * Returns the number of hidden object instances in this state.
	 * @return the number of hideen object instances in this state.
	 */
	public int numHiddenObjects(){
		return hiddenObjectInstances.size();
	}
	
	
	/**
	 * Returns the object in this state with the name oname
	 * @param oname the name of the object instance to return
	 * @return the object instance with the name oname or null if there is no object in this state named oname
	 */
	public ObjectInstance getObject(String oname){
		return objectMap.get(oname);
	}
	
	/**
	 * Returns the observable object instance indexed at position i
	 * @param i the index of the observable object instance to return
	 * @return the observable object instance indexed at position i, or null if i > this.numObservableObjects()
	 */
	public ObjectInstance getObservableObjectAt(int i){
		if(i > objectInstances.size()){
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
		if(i > hiddenObjectInstances.size()){
			return null;
		}
		return hiddenObjectInstances.get(i);
	}
	
	
	/**
	 * Returns the list of observable object instances in this state.
	 * @return the list of observable object instances in this state.
	 */
	public List <ObjectInstance> getObservableObjects(){
		return new ArrayList <ObjectInstance>(objectInstances);
	}
	
	
	/**
	 * Returns the list of hidden object instances in this state.
	 * @return the list of hidden object instances in this state.
	 */
	public List <ObjectInstance> getHiddenObjects(){
		return new ArrayList <ObjectInstance>(hiddenObjectInstances);
	}
	
	
	/**
	 * Returns the list of observable and hidden object instances in this state.
	 * @return the list of observable and hidden object instances in this state.
	 */
	public List <ObjectInstance> getAllObjects(){
		List <ObjectInstance> objects = new ArrayList <ObjectInstance>(objectInstances);
		objects.addAll(hiddenObjectInstances);
		return objects;
	}
	
	/**
	 * Deprecated due to method name change. Instead use {@link #getObjectsOfClass(String)}.
	 */
	@Deprecated
	public List <ObjectInstance> getObjectsOfTrueClass(String oclass){
		List <ObjectInstance> tmp = objectIndexByTrueClass.get(oclass);
		if(tmp == null){
			return new ArrayList <ObjectInstance>();
		}
		return new ArrayList <ObjectInstance>(tmp);
	}


	/**
	 * Returns all objects that belong to the object class named oclass
	 * @param oclass the name of the object class for which objects should be returned
	 * @return all objects that belong to the object class named oclass
	 */
	public List <ObjectInstance> getObjectsOfClass(String oclass){
		List <ObjectInstance> tmp = objectIndexByTrueClass.get(oclass);
		if(tmp == null){
			return new ArrayList <ObjectInstance>();
		}
		return new ArrayList <ObjectInstance>(tmp);
	}
	
	
	/**
	 * Returns the first indexed object of the object class named oclass
	 * @param oclass the name of the object class for which the first indexed object should be returned.
	 * @return the first indexed object of the object class named oclass
	 */
	public ObjectInstance getFirstObjectOfClass(String oclass){
		List <ObjectInstance> obs = this.objectIndexByTrueClass.get(oclass);
		if(obs != null && obs.size() > 0){
			return obs.get(0);
		}
		return null;
	}
	
	/**
	 * Returns a set of of the object class names for all object classes that have instantiated objects in this state.
	 * @return a set of of the object class names for all object classes that have instantiated objects in this state.
	 */
	public Set <String> getObjectClassesPresent(){
		return new HashSet<String>(objectIndexByTrueClass.keySet());
	}
	
	
	/**
	 * Returns a list of list of object instances, grouped by object class
	 * @return a list of list of object instances, grouped by object class
	 */
	public List <List <ObjectInstance>> getAllObjectsByTrueClass(){
		return new ArrayList<List<ObjectInstance>>(objectIndexByTrueClass.values());
	}
	
	
	/**
	 * Returns a string representation of this state using only observable object instances.
	 * @return a string representation of this state using only observable object instances.
	 */
	public String getStateDescription(){
		
		String desc = "";
		for(ObjectInstance o : objectInstances){
			desc = desc + o.getObjectDescription() + "\n";
		}
		
		return desc;
	
	}


	/**
	 * Returns a mapping from object instance names to the list of attributes names that have unset values.
	 * @return a mapping from object instance names to the list of attributes names that have unset values.
	 */
	public Map<String, List<String>> getAllUnsetAttributes(){
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
	
	/**
	 * Returns a string representation of this state using observable and hidden object instances.
	 * @return a string representation of this state using observable and hidden object instances.
	 */
	public String getCompleteStateDescription(){
		
		String desc = "";
		for(ObjectInstance o : objectInstances){
			desc = desc + o.getObjectDescription() + "\n";
		}
		for(ObjectInstance o : hiddenObjectInstances){
			desc = desc + o.getObjectDescription() + "\n";
		}
		
		
		return desc;
		
	}


	/**
	 * Returns a string description of the state with unset attribute values listed as null.
	 * @return a string description of the state with unset attribute values listed as null.
	 */
	public String getCompleteStateDescriptionWithUnsetAttributesAsNull(){
		String desc = "";
		for(ObjectInstance o : objectInstances){
			desc = desc + o.getObjectDesriptionWithNullForUnsetAttributes() + "\n";
		}
		for(ObjectInstance o : hiddenObjectInstances){
			desc = desc + o.getObjectDesriptionWithNullForUnsetAttributes() + "\n";
		}


		return desc;
	}
	
	
	
	/**
	 * Deprecated; use the {@link Action} class' {@link Action#getAllApplicableGroundedActions(State)} method instead.
	 * Returns all GroundedAction objects for the source action a in this state.
	 * @param a the action from which to generate GroundedAction objects.
	 * @return all GroundedAction objects for the source action a in this state.
	 */
	@Deprecated
	public List <GroundedAction> getAllGroundedActionsFor(Action a){
		
		List <GroundedAction> res = new ArrayList<GroundedAction>();
		
		if(a.getParameterClasses().length == 0){
			if(a.applicableInState(this, "")){
				res.add(new GroundedAction(a, new String[]{}));
			}
			return res; //no parameters so just the single ga without params
		}
		
		List <List <String>> bindings = this.getPossibleBindingsGivenParamOrderGroups(a.getParameterClasses(), a.getParameterOrderGroups());
		
		for(List <String> params : bindings){
			String [] aprams = params.toArray(new String[params.size()]);
			if(a.applicableInState(this, aprams)){
				GroundedAction gp = new GroundedAction(a, aprams);
				res.add(gp);
			}
		}
		
		return res;
	}
	
	
	/**
	 * Deprecated; use the {@link Action} class' {@link Action#getAllApplicableGroundedActionsFromActionList(List, State)} method instead.
	 * Returns a list of GroundedAction objects for all grounded actions that can be generated from the provided list of actions.
	 * @param actions the list of actions from which to generate GroudnedAction objects.
	 * @return a list of GroundedAction objects for all grounded actions that can be generated from the provided list of actions.
	 */
	@Deprecated
	public List <GroundedAction> getAllGroundedActionsFor(List <Action> actions){
		List <GroundedAction> res = new ArrayList<GroundedAction>(actions.size());
		for(Action a : actions){
			res.addAll(this.getAllGroundedActionsFor(a));
		}
		return res;
	}
	
	
	
	/**
	 * Deprecated; use the {@link PropositionalFunction} class' {@link PropositionalFunction#getAllGroundedPropsForState(State)} method instead.
	 * Returns all GroundedProp objects for the source propositional function pf in this state.
	 * @param pf the propositional function from which to generate GroundedProp objects.
	 * @return all GroundedProp objects for the source propositional function pf in this state.
	 */
	@Deprecated
	public List <GroundedProp> getAllGroundedPropsFor(PropositionalFunction pf){
		
		List <GroundedProp> res = new ArrayList<GroundedProp>();
		
		if(pf.getParameterClasses().length == 0){
			res.add(new GroundedProp(pf, new String[]{}));
			return res; //no parameters so just the single gp without params
		}
		
		List <List <String>> bindings = this.getPossibleBindingsGivenParamOrderGroups(pf.getParameterClasses(), pf.getParameterOrderGroups());
		
		for(List <String> params : bindings){
			String [] aprams = params.toArray(new String[params.size()]);
			GroundedProp gp = new GroundedProp(pf, aprams);
			res.add(gp);
		}
		
		return res;
	}
	
	
	/**
	 * Deprecated; use the {@link PropositionalFunction} class' {@link PropositionalFunction#somePFGroundingIsTrue(State)} method instead.
	 * Returns whether some GroundedProp of pf is true in this state
	 * @param pf the propositional function to check
	 * @return true if some GroundedProp of pf is true in this state; false otherwise
	 */
	@Deprecated
	public boolean somePFGroundingIsTrue(PropositionalFunction pf){
		List <GroundedProp> gps = this.getAllGroundedPropsFor(pf);
		for(GroundedProp gp : gps){
			if(gp.isTrue(this)){
				return true;
			}
		}
		
		return false;
	}
	
	
	/**
	 * Given an array of parameter object classes and an array of their corresponding parameter order groups,
	 * returns all possible object instance bindings to the parameters, excluding bindings that are equivalent due
	 * to the parameter order grouping.
	 * @param paramClasses the name of object classes to which the bound object instances must belong
	 * @param paramOrderGroups the parameter order group names.
	 * @return A list of all possible object instance bindings for the parameters, were a binding is represented by a list of object instance names
	 */
	public List <List <String>> getPossibleBindingsGivenParamOrderGroups(String [] paramClasses, String [] paramOrderGroups){
		
		List <List <String>> res = new ArrayList <List<String>>();
		List <List <String>> currentBindingSets = new ArrayList <List<String>>();
		List <String> uniqueRenames = this.identifyUniqueClassesInParameters(paramOrderGroups);
		List <String> uniqueParamClases = this.identifyUniqueClassesInParameters(paramClasses);
		
		Map <String, List <ObjectInstance>>	instanceMap = objectIndexByTrueClass;
		
		//first make sure we have objects for each class parameter; if not return empty list
		for(String oclass : uniqueParamClases){
			int n = this.getNumOccurencesOfClassInParameters(oclass, paramClasses);
			List <ObjectInstance> objectsOfClass = instanceMap.get(oclass);
			if(objectsOfClass == null){
				return res;
			}
			if(objectsOfClass.size() < n){
				return res;
			}
		}
		
		this.getPossibleRenameBindingsHelper(res, currentBindingSets, 0, objectInstances, uniqueRenames, paramClasses, paramOrderGroups);
		
		
		return res;
		
	}
	
	
	
	
	@Override
	public String toString(){
		return this.getCompleteStateDescription();
	}
	
	
	
	
	private void getPossibleRenameBindingsHelper(List <List <String>> res, List <List <String>> currentBindingSets, int bindIndex,
			List <ObjectInstance> remainingObjects, List <String> uniqueOrderGroups, String [] paramClasses, String [] paramOrderGroups){
		
		if(bindIndex == uniqueOrderGroups.size()){
			//base case, put it all together and add it to the result
			res.add(this.getBindngFromCombinationSet(currentBindingSets, uniqueOrderGroups, paramOrderGroups));
			return ;
		}
		
		//otherwise we're in the recursive case
		
		String r = uniqueOrderGroups.get(bindIndex);
		String c = this.parameterClassAssociatedWithOrderGroup(r, paramOrderGroups, paramClasses);
		List <ObjectInstance> cands = this.objectsMatchingClass(remainingObjects, c);
		int k = this.numOccurencesOfOrderGroup(r, paramOrderGroups);
		List <List <String>> combs = this.getAllCombinationsOfObjects(cands, k);
		for(List <String> cb : combs){
			
			List <List<String>> nextBinding = new ArrayList<List<String>>(currentBindingSets.size());
			for(List <String> prevBind : currentBindingSets){
				nextBinding.add(prevBind);
			}
			nextBinding.add(cb);
			List <ObjectInstance> nextObsReamining = this.objectListDifference(remainingObjects, cb);
			
			//recursive step
			this.getPossibleRenameBindingsHelper(res, nextBinding, bindIndex+1, nextObsReamining, uniqueOrderGroups, paramClasses, paramOrderGroups);
			
		}
		
		
		
	}
	
	
	private List <ObjectInstance> objectListDifference(List <ObjectInstance> objects, List <String> toRemove){
		List <ObjectInstance> remaining = new ArrayList<ObjectInstance>(objects.size());
		for(ObjectInstance oi : objects){
			String oname = oi.getName();
			if(!toRemove.contains(oname)){
				remaining.add(oi);
			}
		}
		return remaining;
	}
	
	private int getNumOccurencesOfClassInParameters(String className, String [] paramClasses){
		int num = 0;
		for(int i = 0; i < paramClasses.length; i++){
			if(paramClasses[i].equals(className)){
				num++;
			}
		}
		return num;
	}
	
	private List <String> identifyUniqueClassesInParameters(String [] paramClasses){
		List <String> unique = new ArrayList <String>();
		for(int i = 0; i < paramClasses.length; i++){
			if(!unique.contains(paramClasses[i])){
				unique.add(paramClasses[i]);
			}
		}
		return unique;
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
	
	private String parameterClassAssociatedWithOrderGroup(String orderGroup, String [] orderGroups, String [] paramClasses){
		for(int i = 0; i < orderGroups.length; i++){
			if(orderGroups[i].equals(orderGroup)){
				return paramClasses[i];
			}
		}
		return "";
	}
	
	
	private List <ObjectInstance> objectsMatchingClass(List <ObjectInstance> sourceObs, String cname){
		
		List <ObjectInstance> res = new ArrayList<ObjectInstance>(sourceObs.size());
		
		for(ObjectInstance o : sourceObs){
			
			if(o.getTrueClassName().equals(cname)){
				res.add(o);
			}
			
		}
		
		return res;
		
	}
	
	
	
	/**
	 * for a specific parameter order group, return a possible binding
	 * @param comboSets is a list of the bindings for each order group. For instance, if the order groups for each parameter were P, Q, P, Q, R; then there would be three lists
	 * @param orderGroupAssociatedWithSet which order group each list of bindings in comboSets is for
	 * @param orderGroups the parameter order groups for each parameter
	 * @return a binding as a list of object instance names
	 */
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
	
	
	private List <List <String>> getAllCombinationsOfObjects(List <ObjectInstance> objects, int k){
		
		List <List<String>> allCombs = new ArrayList <List<String>>();
		
		int n = objects.size();
		int [] comb = this.initialComb(k, n);
		allCombs.add(this.getListOfBindingsFromCombination(objects, comb));
		while(nextComb(comb, k, n) == 1){
			allCombs.add(this.getListOfBindingsFromCombination(objects, comb));
		}
		
		return allCombs;
		
	}
	
	
	
	private List <String> getListOfBindingsFromCombination(List <ObjectInstance> objects, int [] comb){
		List <String> res = new ArrayList <String>(comb.length);
		for(int i = 0; i < comb.length; i++){
			res.add(objects.get(comb[i]).getName());
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
	
	
	
	
	
}
