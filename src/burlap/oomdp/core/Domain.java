package burlap.oomdp.core;


import java.util.*;

import burlap.oomdp.singleagent.Action;
import burlap.oomdp.stocashticgames.SingleAction;


/**
 * 
 * @author James
 *	This is the base class for an OOMDBP domain. 
 */
public abstract class Domain {
	
	
	protected List <ObjectClass>						objectClasses;			//list of object classes
	protected Map <String, ObjectClass>					objectClassMap;		//look up object classes by name
	
	protected List <Attribute>							attributes;			//list of attributes
	protected Map <String, Attribute>					attributeMap;			//lookup attributes by name
	
	protected List <PropositionalFunction>				propFunctions;			//list of propositional functions
	protected Map <String, PropositionalFunction> 		propFunctionMap;		//lookup propositional functions by name
	

	
	public Domain(){
		
		objectClasses = new ArrayList <ObjectClass>();
		objectClassMap = new HashMap <String, ObjectClass>();
		
		attributes = new ArrayList <Attribute>();
		attributeMap = new HashMap <String, Attribute>();
		
		propFunctions = new ArrayList <PropositionalFunction>();
		propFunctionMap = new HashMap <String, PropositionalFunction>();
		
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
