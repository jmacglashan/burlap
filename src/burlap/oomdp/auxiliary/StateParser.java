package burlap.oomdp.auxiliary;

import burlap.oomdp.core.State;

public interface StateParser {

	public String stateToString(State s);
	public State stringToState(String str);
	
	
}
