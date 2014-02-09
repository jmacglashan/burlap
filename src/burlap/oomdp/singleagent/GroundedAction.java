package burlap.oomdp.singleagent;

import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;

/**
 * A grounded action contains a reference to an action and the names of object instances that
 * are bound to the Action's parameters.
 * @author James
 *
 */
public class GroundedAction extends AbstractGroundedAction{

	/**
	 * The action object for this grounded action
	 */
	public Action action;
	
	
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


	@Override
	public boolean isExcutable() {
		return true;
	}


	@Override
	public boolean actionDomainIsObjectIdentifierDependent() {
		return this.action.domain.isObjectIdentifierDependent();
	}


	@Override
	public AbstractGroundedAction copy() {
		return new GroundedAction(action, params);
	}
	
}
