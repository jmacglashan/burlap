package burlap.oomdp.singleagent.common;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.singleagent.Action;

import java.util.List;


/**
 * @author James
 * This action is an action that does nothing. 
 * It may be useful for making references to actions that do not have domain associations
 * or if a domain needs a no-op action
 * 
 */
public class NullAction extends Action {

	
	public NullAction(String name){
		this.name = name;
		this.parameterClasses = new String[0];
		this.parameterOrderGroup = new String[0];
		this.domain = null;
	}
	
	public NullAction(String name, Domain domain, String parameterClasses){
		super(name, domain, parameterClasses);
	}
	
	public NullAction(String name, Domain domain, String [] parameterClasses){
		super(name, domain, parameterClasses);
	}
	
	public NullAction(String name, Domain domain, String [] parameterClasses, String [] replacedClassName){
		super(name, domain, parameterClasses, replacedClassName);
	}
	
	@Override
	protected State performActionHelper(State st, String[] params) {
		return st;
	}

	@Override
	public List<TransitionProbability> getTransitions(State s, String[] params) {
		return this.deterministicTransition(s, params);
	}
}
