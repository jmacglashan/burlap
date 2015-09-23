package burlap.oomdp.singleagent.common;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.states.State;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.FullActionModel;
import burlap.oomdp.singleagent.GroundedAction;

import java.util.List;


/**
 * @author James
 * This action is an action that does nothing. 
 * It may be useful for making references to actions that do not have domain associations
 * or if a domain needs a no-op action
 * 
 */
public class NullAction extends SimpleAction.SimpleDeterministicAction implements FullActionModel {

	
	public NullAction(String name){
		this.name = name;
		this.domain = null;
	}
	
	public NullAction(String name, Domain domain){
		super(name, domain);
	}

	
	@Override
	protected State performActionHelper(State st, GroundedAction groundedAction) {
		return st;
	}

}
