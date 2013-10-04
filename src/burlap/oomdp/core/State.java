package burlap.oomdp.core;

import java.util.*;

import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;

public class State {

	
	private List <ObjectInstance>							objectInstances;			//list of observable object instances that define the state
	private List <ObjectInstance>							hiddenObjectInstances;		//list of hidden object instances that facilitate domain dynamics and infer observable values
	private Map <String, ObjectInstance>					objectMap;					//map from object names to their instances
	
	private Map <String, List <ObjectInstance>>				objectIndexByClass;			//map of object instances organized by (pseudo)class name
	private Map <String, List <ObjectInstance>>				objectIndexByTrueClass;		//map of object instances organized by true class name

	
	
	public State(){
		this.initDataStructures();
	}
	
	
	public State(State s){
		
		
		this.initDataStructures();
		
		for(ObjectInstance o : s.objectInstances){
			this.addObject(o.copy());
		}
		
		for(ObjectInstance o : s.hiddenObjectInstances){
			this.addObject(o.copy());
		}
		
	}
	
	public State copy(){
		return new State(this);
	}
	
	public void initDataStructures(){
		
		objectInstances = new ArrayList <ObjectInstance>();
		hiddenObjectInstances = new ArrayList <ObjectInstance>();
		objectMap = new HashMap <String, ObjectInstance>();
		
		objectIndexByClass = new HashMap <String, List <ObjectInstance>>();
		
		objectIndexByTrueClass = new HashMap <String, List <ObjectInstance>>();
	}
	
	
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
		
		String oclass = o.getPseudoClass();
		String otclass = o.getTrueClassName();
		
		//manage pseudo indexing
		if(objectIndexByClass.containsKey(oclass)){
			objectIndexByClass.get(oclass).add(o);
		}
		else{
			
			ArrayList <ObjectInstance> classList = new ArrayList <ObjectInstance>();
			classList.add(o);
			objectIndexByClass.put(oclass, classList);
			
		}
		
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
	
	
	public void removeObject(String oname){
		this.removeObject(objectMap.get(oname));
	}
	
	
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
		
		String oclass = o.getPseudoClass();
		List <ObjectInstance> classList = objectIndexByClass.get(oclass);
		
		String otclass = o.getTrueClassName();
		List <ObjectInstance> classTList = objectIndexByTrueClass.get(otclass);
		
		//manage psuedo class
		
		//if this index has more than one entry, then we can just remove from it and be done
		if(classList.size() > 1){
			classList.remove(o);
		}
		else{
			//otherwise we have to remove class entries for it since it's the only one
			objectIndexByClass.remove(oclass);
		}
		
		
		//manage true class
		
		//if this index has more than one entry, then we can just remove from it and be done
		if(classTList.size() > 1){
			classTList.remove(o);
		}
		else{
			//otherwise we have to remove class entries for it
			objectIndexByTrueClass.remove(otclass);
		}
		
		
		
	}
	
	
	public void renameObject(String originalName, String newName){
		ObjectInstance o = objectMap.get(originalName);
		o.setName(newName);
		objectMap.remove(originalName);
		objectMap.put(newName, o);
	}
	
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
	
	
	public State parameterize(String [] params){
		if(params.length == 0){
			return this; //no parameters, it's the same state
		}
		
		//otherwise label them accordingly
		for(int i = 0; i < params.length; i++){
			this.parameterize(params[i], "param" + i);
		}
		
		return this;
		
	}
	
	public State parameterize(String [] params, String [] newClassNames){
		if(params.length == 0){
			return this; //no parameters, it's the same state
		}
		
		//otherwise label them accordingly
		for(int i = 0; i < params.length; i++){
			this.parameterize(params[i], newClassNames[i]);
		}
		
		return this;
		
	}
	
	public State deparameterize(String [] params){
		if(params.length == 0){
			return this; //no parameters, it's the same state
		}
		
		//otherwise label them accordingly
		for(int i = 0; i < params.length; i++){
			this.deparameterize(params[i]);
		}
		
		return this;
		
	}
	
	public void parameterize(String oname, String pseudoClassName){
		
		ObjectInstance o = objectMap.get(oname);
		
		if(o == null){
			return ;
		}
		
		//first remove the object indexing under its previous class
		this.removeObjectClassIndexing(o);
		
		//now change the class name
		o.pushPseudoClass(pseudoClassName);
		
		//now reorganize the object indexing by its new class
		this.addObjectClassIndexing(o);
		
	}
	
	public void deparameterize(String oname){
		
		ObjectInstance o = objectMap.get(oname);
		
		if(o == null){
			return ;
		}
		
		//first remove the object indexing under its current class
		this.removeObjectClassIndexing(o);
		
		//now change the class name to be what it was before this last parameterization
		o.popPseudoClass();
		
		//now reorganize the object indexing by its new class
		this.addObjectClassIndexing(o);
		
	}
	
	
	
	public int numTotalObjets(){
		return objectInstances.size() + hiddenObjectInstances.size();
	}
	
	public int numObservableObjects(){
		return objectInstances.size();
	}
	
	public int numHiddenObjects(){
		return hiddenObjectInstances.size();
	}
	
	public ObjectInstance getObject(String oname){
		return objectMap.get(oname);
	}
	
	public ObjectInstance getObservableObjectAt(int i){
		if(i > objectInstances.size()){
			return null;
		}
		return objectInstances.get(i);
	}
	
	public ObjectInstance getHiddenObjectAt(int i){
		if(i > hiddenObjectInstances.size()){
			return null;
		}
		return hiddenObjectInstances.get(i);
	}
	
	public List <ObjectInstance> getObservableObjects(){
		return new ArrayList <ObjectInstance>(objectInstances);
	}
	
	public List <ObjectInstance> getHiddenObjects(){
		return new ArrayList <ObjectInstance>(hiddenObjectInstances);
	}
	
	public List <ObjectInstance> getAllObjects(){
		List <ObjectInstance> objects = new ArrayList <ObjectInstance>(objectInstances);
		objects.addAll(hiddenObjectInstances);
		return objects;
	}
	
	public List <ObjectInstance> getObjectsOfClass(String oclass){
		List <ObjectInstance> tmp = objectIndexByClass.get(oclass);
		if(tmp == null){
			return new ArrayList <ObjectInstance>();
		}
		return new ArrayList <ObjectInstance>(tmp);
	}
	
	public List <ObjectInstance> getObjectsOfTrueClass(String oclass){
		List <ObjectInstance> tmp = objectIndexByTrueClass.get(oclass);
		if(tmp == null){
			return new ArrayList <ObjectInstance>();
		}
		return new ArrayList <ObjectInstance>(tmp);
	}
	
	public List <List <ObjectInstance>> getAllObjectsByClass(){
		return new ArrayList<List<ObjectInstance>>(objectIndexByClass.values());
	}
	
	public List <List <ObjectInstance>> getAllObjectsByTrueClass(){
		return new ArrayList<List<ObjectInstance>>(objectIndexByTrueClass.values());
	}
	
	public Set <String> getObjectClassesPresent(){
		return new HashSet<String>(objectIndexByClass.keySet());
	}
	
	public String getStateDescription(){
		
		String desc = "";
		for(ObjectInstance o : objectInstances){
			desc = desc + o.getObjectDescription() + "\n";
		}
		
		return desc;
	
	}
	
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
	
	
	
	
	public List <GroundedAction> getAllGroundedActionsFor(Action a){
		
		List <GroundedAction> res = new ArrayList<GroundedAction>();
		
		if(a.getParameterClasses().length == 0){
			if(a.applicableInState(this, "")){
				res.add(new GroundedAction(a, new String[]{}));
			}
			return res; //no parameters so just the single ga without params
		}
		
		List <List <String>> bindings = this.getPossibleBindingsGivenParamOrderGroups(a.getParameterClasses(), a.getParameterOrderGroups(), true);
		
		for(List <String> params : bindings){
			String [] aprams = params.toArray(new String[params.size()]);
			if(a.applicableInState(this, aprams)){
				GroundedAction gp = new GroundedAction(a, aprams);
				res.add(gp);
			}
		}
		
		return res;
	}
	
	public List <GroundedAction> getAllGroundedActionsFor(List <Action> actions){
		List <GroundedAction> res = new ArrayList<GroundedAction>(actions.size());
		for(Action a : actions){
			res.addAll(this.getAllGroundedActionsFor(a));
		}
		return res;
	}
	
	
	public List <GroundedProp> getAllGroundedPropsFor(PropositionalFunction pf){
		
		List <GroundedProp> res = new ArrayList<GroundedProp>();
		
		if(pf.getParameterClasses().length == 0){
			res.add(new GroundedProp(pf, new String[]{}));
			return res; //no parameters so just the single gp without params
		}
		
		List <List <String>> bindings = this.getPossibleBindingsGivenParamOrderGroups(pf.getParameterClasses(), pf.getParameterOrderGroups(), true);
		
		for(List <String> params : bindings){
			String [] aprams = params.toArray(new String[params.size()]);
			GroundedProp gp = new GroundedProp(pf, aprams);
			res.add(gp);
		}
		
		return res;
	}
	
	
	public boolean somePFGroundingIsTrue(PropositionalFunction pf){
		List <GroundedProp> gps = this.getAllGroundedPropsFor(pf);
		for(GroundedProp gp : gps){
			if(gp.isTrue(this)){
				return true;
			}
		}
		
		return false;
	}
	
	
	public List <List <String>> getPossibleBindingsGivenParamOrderGroups(String [] paramClasses, String [] paramOrderGroups, boolean useTrueClass){
		
		List <List <String>> res = new ArrayList <List<String>>();
		List <List <String>> currentBindingSets = new ArrayList <List<String>>();
		List <String> uniqueRenames = this.identifyUniqueClassesInParameters(paramOrderGroups);
		List <String> uniqueParamClases = this.identifyUniqueClassesInParameters(paramClasses);
		
		Map <String, List <ObjectInstance>>	instanceMap = objectIndexByClass;
		if(useTrueClass){
			instanceMap = objectIndexByTrueClass;
		}
		
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
		
		this.getPossibleRenameBindingsHelper(res, currentBindingSets, 0, objectInstances, uniqueRenames, paramClasses, paramOrderGroups, useTrueClass);
		
		
		return res;
		
	}
	
	
	
	
	
	
	
	
	
	private void getPossibleRenameBindingsHelper(List <List <String>> res, List <List <String>> currentBindingSets, int bindIndex,
			List <ObjectInstance> remainingObjects, List <String> uniqueOrderGroups, String [] paramClasses, String [] paramOrderGroups,
			boolean useTrueClass){
		
		if(bindIndex == uniqueOrderGroups.size()){
			//base case, put it all together and add it to the result
			res.add(this.getBindngFromCombinationSet(currentBindingSets, uniqueOrderGroups, paramOrderGroups));
			return ;
		}
		
		//otherwise we're in the recursive case
		
		String r = uniqueOrderGroups.get(bindIndex);
		String c = this.parameterClassAssociatedWithOrderGroup(r, paramOrderGroups, paramClasses);
		List <ObjectInstance> cands = this.objectsMatchingClass(remainingObjects, c, useTrueClass);
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
			this.getPossibleRenameBindingsHelper(res, nextBinding, bindIndex+1, nextObsReamining, uniqueOrderGroups, paramClasses, paramOrderGroups, useTrueClass);
			
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
	
	
	private List <ObjectInstance> objectsMatchingClass(List <ObjectInstance> sourceObs, String cname, boolean useTrueClass){
		
		List <ObjectInstance> res = new ArrayList<ObjectInstance>(sourceObs.size());
		
		for(ObjectInstance o : sourceObs){
			if(useTrueClass){
				if(o.getTrueClassName().equals(cname)){
					res.add(o);
				}
			}
			else{
				if(o.getPseudoClass().equals(cname)){
					res.add(o);
				}
			}
		}
		
		return res;
		
	}
	
	
	
	//for renaming parameter listing
	//comboSets: is a list of the bindings for each order group. For instance, if the order groups for each parameter were P, Q, P, Q, R; then there would be three lists
	//orderGroupAssociatedWithSet: which order group each list of bindings in comboSets is for
	//paramRenames: the parameter order groups for each parameter
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
