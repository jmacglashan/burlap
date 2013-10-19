package burlap.oomdp.core;


import java.util.*;

import burlap.oomdp.singleagent.Action;
import burlap.oomdp.stocashticgames.SingleAction;


/**
 * This is the base class for an OO-MDP/OO-SG domain. It includes data members for the set of attributes, object classes
 * and propositional functions of the domain.
 * @author James MacGlashan
 */
public abstract class Domain {
	
	
	protected List <ObjectClass>						objectClasses;			//list of object classes
	protected Map <String, ObjectClass>					objectClassMap;			//look up object classes by name
	
	protected List <Attribute>							attributes;				//list of attributes
	protected Map <String, Attribute>					attributeMap;			//lookup attributes by name
	
	protected List <PropositionalFunction>				propFunctions;			//list of propositional functions
	protected Map <String, PropositionalFunction> 		propFunctionMap;		//lookup propositional functions by name
	
	protected boolean									nameDependentDomain = false;
	

	
	public Domain(){
		
		objectClasses = new ArrayList <ObjectClass>();
		objectClassMap = new HashMap <String, ObjectClass>();
		
		attributes = new ArrayList <Attribute>();
		attributeMap = new HashMap <String, Attribute>();
		
		propFunctions = new ArrayList <PropositionalFunction>();
		propFunctionMap = new HashMap <String, PropositionalFunction>();
		
	}
	
	
	/**
	 * Sets whether this domain's states are object name dependent or independent. In an OO-MDP states are represented
	 * as a set of object instances; therefore state equality can either be determined by whether there is a
	 * bijection between the states such that the matched objects have the same value (name independent), or whether the same
	 * object references have the same values (name dependent). For instance, imagine a state s_1 with two objects of the same class,
	 * o_1 and o_2 with value assignments v_a and v_b, respectively. Imagine a corresponding state s_2, also with objects o_1 and
	 * o_2; however, in s_2, the value assignment is o_1=v_b and o_2=v_a. If the domain is name independent, then s_1 == s_2,
	 * because you can match o_1 in s_1 to o_2 in s_2 (and symmetrically for the other objects). However, if the domain is
	 * name dependent, then s_1 != s_2, because the specific object references have different values in each state.
	 * @param nameDependent sets whether this domain's states are object name dependent (true) or not (false).
	 */
	public void setNameDependence(boolean nameDependent){
		this.nameDependentDomain = nameDependent;
	}
	
	
	/**
	 * Returns whether this domain's states are object name dependent. In an OO-MDP states are represented
	 * as a set of object instances; therefore state equality can either be determined by whether there is a
	 * bijection between the states such that the matched objects have the same value (name independent), or whether the same
	 * object references have the same values (name dependent). For instance, imagine a state s_1 with two objects of the same class,
	 * o_1 and o_2 with value assignments v_a and v_b, respectively. Imagine a corresponding state s_2, also with objects o_1 and
	 * o_2; however, in s_2, the value assignment is o_1=v_b and o_2=v_a. If the domain is name independent, then s_1 == s_2,
	 * because you can match o_1 in s_1 to o_2 in s_2 (and symmetrically for the other objects). However, if the domain is
	 * name dependent, then s_1 != s_2, because the specific object references have different values in each state.
	 * @return true if this domain is name dependent and false if it object name independent.
	 */
	public boolean isNameDependent(){
		return this.nameDependentDomain;
	}
	
	
	/**
	 * Will return a new instance of this Domain's class (either SADomain or SGDomain)
	 * @return a new instance of this Domain's class (either SADomain or SGDomain)
	 */
	protected abstract Domain newInstance();
	
	/**
	 * This will return a new domain object populated with copies of this Domain's ObjectClasses. Note that propositional
	 * functions and actions are not copied into the new domain
	 * @return a new Domain object with copies of this Domain's ObjectClasses
	 */
	public Domain getNewDomainWithCopiedObjectClasses(){
		Domain d = this.newInstance();
		for(Attribute a : this.attributes){
			a.copy(d);
		}
		
		return d;
	}
	
	public void addObjectClass(ObjectClass oc){
		if(!objectClassMap.containsKey(oc.name)){
			objectClasses.add(oc);
			objectClassMap.put(oc.name, oc);
		}
	}
	
	public void addAttribute(Attribute att){
		if(!attributeMap.containsKey(att.name)){
			attributes.add(att);
			attributeMap.put(att.name, att);
		}
	}
	
	public void addPropositionalFunction(PropositionalFunction prop){
		if(!propFunctionMap.containsKey(prop.getName())){
			propFunctions.add(prop);
			propFunctionMap.put(prop.getName(), prop);
		}
	}
	
	
	public abstract void addAction(Action act);
	public abstract void addSingleAction(SingleAction sa);
	
	
	public List <ObjectClass> getObjectClasses(){
		return new ArrayList <ObjectClass>(objectClasses);
	}
	
	public ObjectClass getObjectClass(String name){
		return objectClassMap.get(name);
	}
		
	public List <Attribute> getAttributes(){
		return new ArrayList <Attribute>(attributes);
	}
	
	public Attribute getAttribute(String name){
		return attributeMap.get(name);
	}
	
	
	public List <PropositionalFunction> getPropFunctions(){
		return new ArrayList <PropositionalFunction>(propFunctions);
	}
	
	public PropositionalFunction getPropFunction(String name){
		return propFunctionMap.get(name);
	}
	
	
	public abstract List <Action> getActions();
	public abstract List <SingleAction> getSingleActions();
	
	public abstract Action getAction(String name);
	public abstract SingleAction getSingleAction(String name);
	
	// Maps propFuncClass -> propList
	// eg: color -> isWhite, isBlue, isYellow...
	public Map<String, Set<PropositionalFunction>> getPropositionlFunctionsMap() {
		HashMap<String, Set<PropositionalFunction>> propFuncs = new HashMap<String, Set<PropositionalFunction>>();
		for(PropositionalFunction pf : this.propFunctions) {

			String propFuncClass = pf.getClassName();
			Set<PropositionalFunction> propList = propFuncs.get(propFuncClass);
			if(propList == null) {
				propList = new HashSet<PropositionalFunction>();
			}

			propList.add(pf);
			propFuncs.put(propFuncClass, propList);
			
		}
		return propFuncs;
	}

	public Map<String, Set<PropositionalFunction>> getPropositionlFunctionsFromObjectClass(String objectName) {
		HashMap<String, Set<PropositionalFunction>> propFuncs = new HashMap<String, Set<PropositionalFunction>>();
		for(PropositionalFunction pf : this.propFunctions) {
			for(String paramClass : pf.getParameterClasses()) {
				if(paramClass.equals(objectName)) {
					String propFuncClass = pf.getClassName();
					Set<PropositionalFunction> propList = propFuncs.get(propFuncClass);
					if(propList == null) {
						propList = new HashSet<PropositionalFunction>();
					}

					propList.add(pf);
					propFuncs.put(propFuncClass, propList);
				}
			}
		}
		return propFuncs;
	}

}
