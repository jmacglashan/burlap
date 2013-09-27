package burlap.oomdp.stocashticgames.common;

import burlap.oomdp.core.State;
import burlap.oomdp.stocashticgames.SGDomain;
import burlap.oomdp.stocashticgames.SingleAction;

public class UniversalSingleAction extends SingleAction {

	public UniversalSingleAction(SGDomain d, String name) {
		super(d, name);
	}
	
	public UniversalSingleAction(SGDomain d, String name, String [] types){
		super(d, name, types);
	}
	
	public UniversalSingleAction(SGDomain d, String name, String [] types, String [] renames){
		super(d, name, types, renames);
	}

	@Override
	public boolean isApplicableInState(State s, String actingAgent, String [] params) {
		return true;
	}

}
