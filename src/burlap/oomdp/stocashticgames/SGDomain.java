package burlap.oomdp.stocashticgames;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.singleagent.Action;


public class SGDomain extends Domain{

	protected Set <SingleAction>						singleActions;			//actions that each individual agent can take
	protected Map <String, SingleAction>				singleActionMap;		
	
	public SGDomain() {
		objectClasses = new ArrayList <ObjectClass>();
		objectClassMap = new HashMap <String, ObjectClass>();
		
		attributes = new ArrayList <Attribute>();
		attributeMap = new HashMap <String, Attribute>();
		
		propFunctions = new ArrayList <PropositionalFunction>();
		propFunctionMap = new HashMap <String, PropositionalFunction>();
		
		singleActions = new HashSet<SingleAction>();
		singleActionMap = new HashMap<String, SingleAction>();
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
	
	
	@Override
	public void addSingleAction(SingleAction sa){
		singleActions.add(sa);
		singleActionMap.put(sa.actionName, sa);
	}


	
	
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
	
	
	@Override
	public List <SingleAction> getSingleActions(){
		return new ArrayList<SingleAction>(singleActions);
	}
	
	
	@Override
	public SingleAction getSingleAction(String name){
		return singleActionMap.get(name);
	}



	@Override
	public void addAction(Action act) {
		throw new UnsupportedOperationException("Stochastic Games domain cannot add actions designed for single agent formalisms");
	}



	@Override
	public List<Action> getActions() {
		throw new UnsupportedOperationException("Stochastic Games domain does not contain any action for single agent formalisms");
	}



	@Override
	public Action getAction(String name) {
		throw new UnsupportedOperationException("Stochastic Games domain does not contain any action for single agent formalisms");
	}
	
	

}
