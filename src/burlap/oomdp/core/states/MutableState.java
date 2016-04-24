package burlap.oomdp.core.states;

import java.util.*;

import burlap.oomdp.core.objects.ObjectInstance;


/**
 * State objects are a collection of Object Instances.
 * @author James MacGlashan
 *
 */
public class MutableState extends OOMDPState implements State{

	
	/**
	 * List of object instances that define the state
	 */
	protected List <ObjectInstance>							objectInstances;

	
	/**
	 * Map from object names to their instances
	 */
	protected Map <String, ObjectInstance>					objectMap;
	
	
	/**
	 * Map of object instances organized by class name
	 */
	protected Map <String, List <ObjectInstance>> 			objectIndexByClass;

	
	
	public MutableState(){
		super();
		this.initDataStructures();
	}
	
	
	/**
	 * Initializes this state as a deep copy of the object instances in the provided source state s
	 * @param s the source state from which this state will be initialized.
	 */
	public MutableState(MutableState s){
		super();
		
		this.initDataStructures();
		
		for(ObjectInstance o : s.objectInstances){
			this.addObject(o.copy());
		}

		
	}
	
	/**
	 * Returns a deep copy of this state.
	 * @return a deep copy of this state.
	 */
	@Override
	public MutableState copy(){
		return new MutableState(this);
	}
	
	
	/**
	 * Performs a semi-deep copy of the state in which only the objects with the names in deepCopyObjectNames are deep copied and the rest of the
	 * objects are shallowed copied.
	 * @param deepCopyObjectNames the names of the objects to be deep copied.
	 * @return a new state that is a mix of a shallow and deep copy of this state.
	 */
	public MutableState semiDeepCopy(String...deepCopyObjectNames){
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
	public MutableState semiDeepCopy(ObjectInstance...deepCopyObjects){
		
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
	public MutableState semiDeepCopy(Set<ObjectInstance> deepCopyObjects){
		
		MutableState s = new MutableState();
		for(ObjectInstance o : this.objectInstances){
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
		objectMap = new HashMap <String, ObjectInstance>();
		objectIndexByClass = new HashMap <String, List <ObjectInstance>>();
	}
	
	
	/**
	 * Adds object instance o to this state.
	 * @param o the object instance to be added to this state.
	 */
	public State addObject(ObjectInstance o){
		
		String oname = o.getName();
		
		if(objectMap.containsKey(oname)){
			return this; //don't add an object that conflicts with another object of the same name
		}
		
		
		objectMap.put(oname, o);
		objectInstances.add(o);

		
		
		this.addObjectClassIndexing(o);
		
		return this;
	}
	
	public State addAllObjects(Collection<ObjectInstance> objects) {
		for (ObjectInstance object : objects) {
			this.addObject(object);
		}
		return this;
	}
	
	private void addObjectClassIndexing(ObjectInstance o){
		
		String otclass = o.getClassName();
		
		//manage true indexing
		if(objectIndexByClass.containsKey(otclass)){
			objectIndexByClass.get(otclass).add(o);
		}
		else{
			
			ArrayList <ObjectInstance> classList = new ArrayList <ObjectInstance>();
			classList.add(o);
			objectIndexByClass.put(otclass, classList);
			
		}
		
	}
	
	
	/**
	 * Removes the object instance with the name oname from this state.
	 * @param oname the name of the object instance to remove.
	 */
	public State removeObject(String oname){
		this.removeObject(objectMap.get(oname));
		return this;
	}
	
	
	/**
	 * Removes the object instance o from this state.
	 * @param o the object instance to remove from this state.
	 */
	public State removeObject(ObjectInstance o){
		if(o == null){
			return this;
		}
		
		String oname = o.getName();
		
		if(!objectMap.containsKey(oname)){
			return this; //make sure we're removing something that actually exists in this state!
		}
		

		objectInstances.remove(o);

		
		objectMap.remove(oname);
		
		this.removeObjectClassIndexing(o);
		return this;
	}
	
	public State removeAllObjects(Collection<ObjectInstance> objects) {
		for (ObjectInstance object : objects){ 
			this.removeObject(object);
		}
		return this;
	}
	
	private void removeObjectClassIndexing(ObjectInstance o){
		
		
		String otclass = o.getClassName();
		List <ObjectInstance> classTList = objectIndexByClass.get(otclass);
		
		//if this index has more than one entry, then we can just remove from it and be done
		if(classTList.size() > 1){
			classTList.remove(o);
		}
		else{
			//otherwise we have to remove class entries for it
			objectIndexByClass.remove(otclass);
		}
		
		
		
	}
	
	
	
	
	
	/**
	 * Renames the identifier for object instance o in this state to newName.
	 * @param o the object instance to rename in this state
	 * @param newName the new name of the object instance
	 */
	public State renameObject(ObjectInstance o, String newName){
		String originalName = o.getName();
		o.setName(newName);
		objectMap.remove(originalName);
		objectMap.put(newName, o);
		return this;
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
		
		for(List <ObjectInstance> objects : objectIndexByClass.values()){
			
			String oclass = objects.get(0).getClassName();
			List <ObjectInstance> oobjects = so.getObjectsOfClass(oclass);
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((objectIndexByClass == null) ? 0 : objectIndexByClass
                        .hashCode());
        result = prime * result
                + ((objectInstances == null) ? 0 : objectInstances.hashCode());
        result = prime * result
                + ((objectMap == null) ? 0 : objectMap.hashCode());
        return result;
    }


	@Override
    public boolean equals(Object other){
    
        if(this == other){
            return true;
        }
        
        if(!(other instanceof MutableState)){
            return false;
        }
        
        MutableState so = (MutableState)other;
        
        if(this.numTotalObjects() != so.numTotalObjects()){
            return false;
        }
        
        Set<String> matchedObjects = new HashSet<String>();
        for(List <ObjectInstance> objects : objectIndexByClass.values()){
            
            String oclass = objects.get(0).getClassName();
            List <ObjectInstance> oobjects = so.getObjectsOfClass(oclass);
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
	 * Returns the number of object instances in this state.
	 * @return the number of object instances in this state.
	 */
	public int numTotalObjects(){
		return objectInstances.size();
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
	 * Returns the list of observable and hidden object instances in this state.
	 * @return the list of observable and hidden object instances in this state.
	 */
	public List <ObjectInstance> getAllObjects(){
		List <ObjectInstance> objects = new ArrayList <ObjectInstance>(objectInstances);
		return objects;
	}
	
	/**
	 * Returns all objects that belong to the object class named oclass
	 * @param oclass the name of the object class for which objects should be returned
	 * @return all objects that belong to the object class named oclass
	 */
	public List <ObjectInstance> getObjectsOfClass(String oclass){
		List <ObjectInstance> tmp = objectIndexByClass.get(oclass);
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
		List <ObjectInstance> obs = this.objectIndexByClass.get(oclass);
		if(obs != null && !obs.isEmpty()){
			return obs.get(0);
		}
		return null;
	}
	
	/**
	 * Returns a set of of the object class names for all object classes that have instantiated objects in this state.
	 * @return a set of of the object class names for all object classes that have instantiated objects in this state.
	 */
	public Set <String> getObjectClassesPresent(){
		return new HashSet<String>(objectIndexByClass.keySet());
	}
	
	
	/**
	 * Returns a list of list of object instances, grouped by object class
	 * @return a list of list of object instances, grouped by object class
	 */
	public List <List <ObjectInstance>> getAllObjectsByClass(){
		return new ArrayList<List<ObjectInstance>>(objectIndexByClass.values());
	}
	



	/**
	 * Returns a mapping from object instance names to the list of attributes names that have unset values.
	 * @return a mapping from object instance names to the list of attributes names that have unset values.
	 */
	public Map<String, List<String>> getAllUnsetAttributes(){
		Map<String, List<String>> unset = new HashMap<String, List<String>>();
		for(ObjectInstance o : this.objectInstances){
			List<String> unsetA = o.unsetAttributes();
			if(!unsetA.isEmpty()){
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
		
		
		return desc;
		
	}


	/**
	 * Returns a string description of the state with unset attribute values listed as null.
	 * @return a string description of the state with unset attribute values listed as null.
	 */
	public String getCompleteStateDescriptionWithUnsetAttributesAsNull(){
		String desc = "";
		for(ObjectInstance o : objectInstances){
			desc = desc + o.getObjectDescriptionWithNullForUnsetAttributes() + "\n";
		}


		return desc;
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
		
		Map <String, List <ObjectInstance>>	instanceMap = objectIndexByClass;
		
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
			
			if(o.getClassName().equals(cname)){
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
