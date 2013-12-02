package burlap.oomdp.singleagent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

/**
 * A grounded action contains a reference to an action and the names of object instances that
 * are bound to the Action's parameters.
 * @author James
 *
 */
public class GroundedAction {

	public Action action;
	public String [] params;
	
	
	/**
	 * Initializes the GroundedAction with the given Action reference and action parameters.
	 * @param a the action reference
	 * @param p a String array of object parameters for the action
	 */
	public GroundedAction(Action a, String [] p){
		this.init(a, p);
	}
	
	
	/**
	 * Initializes the GroundedAction with the given Action reference and action parameters.
	 * @param a the action reference
	 * @param p a comma delineated String representing the object parameters for the action.
	 */
	public GroundedAction(Action a, String p){
		
		String [] ps = null;
		if(p.equals("")){
			ps = new String[0];
		}
		else{
			ps = p.split(",");
		}
		this.init(a, ps);
	}
	
	
	private void init(Action a, String [] p){
		action = a;
		params = p;
	}
	
	/**
	 * Executes the grounded action on a given state
	 * @param s the state on which to execute the action
	 * @return The state after the action has been executed
	 */
	public State executeIn(State s){
		return action.performAction(s, params);
	}
	
	/**
	 * Returns the action name for this grounded action.
	 * @return the action name for this grounded action.
	 */
	public String actionName(){
		return this.action.getName();
	}
	
	/**
	 * This method will translate this object's parameters that were assigned for a given source state, into object parameters in the
	 * target state that are equal. This method is useful if a domain uses parameterized actions and is object identifier invariant.
	 * If the domain of this grounded aciton's action is object identifier dependent, then no translation will occur
	 * and this object will be returned. This object will also be returned if it is a parameterless action.
	 * @param sourceState the source state from which this objects parameters were bound.
	 * @param targetState a target state with potentially different object identifiers for equivalent values.
	 * @return a grounded action object whose parameters have been translated to the target state object identifiers
	 */
	public GroundedAction translateParameters(State sourceState, State targetState){
		
		if(this.params.length == 0 || this.action.domain.isObjectIdentifierDependent()){
			//no need to translate a parameterless action or an action that belongs to a name dependent domain
			return this;
		}
		
		Set <String> matchedObjects = new HashSet<String>();
		String [] nparams = new String[this.params.length];
		int i = 0;
		for(String oname : this.params){
			ObjectInstance o = sourceState.getObject(oname);
			List<ObjectInstance> cands = targetState.getObjectsOfTrueClass(o.getObjectClass().name);
			for(ObjectInstance cand : cands){
				if(matchedObjects.contains(cand.getName())){
					continue ;
				}
				if(o.valueEquals(cand)){
					nparams[i] = o.getName();
					matchedObjects.add(o.getName());
					break;
				}
			}
			
			i++;
		}
		
		return new GroundedAction(action, nparams);
	}
	

	@Override
	public String toString(){
		StringBuffer buf = new StringBuffer();
		buf.append(action.getName());
		for(int i = 0; i < params.length; i++){
			buf.append(" ").append(params[i]);
		}
		
		return buf.toString();
	}
	
	@Override
	public int hashCode(){
		return this.action.getName().hashCode();
	}
	
	
	@Override
	public boolean equals(Object other){
		
		if(this == other){
			return true;
		}
		
		if(!(other instanceof GroundedAction)){
			return false;
		}
		
		GroundedAction go = (GroundedAction)other;
		if(!this.action.getName().equals(go.action.getName())){
			return false;
		}
		
		String [] pog = this.action.getParameterOrderGroups();
		
		for(int i = 0; i < this.params.length; i++){
			String p = this.params[i];
			String orderGroup = pog[i];
			boolean foundMatch = false;
			for(int j = 0; j < go.params.length; j++){
				if(p.equals(go.params[j]) && orderGroup.equals(pog[j])){
					foundMatch = true;
					break;
				}
			}
			if(!foundMatch){
				return false;
			}		
		}
		
		return true;
	}
	
}
