package burlap.mdp.singleagent.common;

import burlap.mdp.core.Domain;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.FullActionModel;
import burlap.mdp.singleagent.GroundedAction;


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
	protected State sampleHelper(State st, GroundedAction groundedAction) {
		return st;
	}

}
