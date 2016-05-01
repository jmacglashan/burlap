package burlap.oomdp.singleagent.common;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.state.State;
import burlap.oomdp.singleagent.FullActionModel;
import burlap.oomdp.singleagent.GroundedAction;


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
	}
	
	public NullAction(String name, Domain domain){
		super(name, domain);
	}

	
	@Override
	protected State performActionHelper(State st, GroundedAction groundedAction) {
		return st;
	}

}
