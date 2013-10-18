package burlap.behavior.statehashing;

import burlap.oomdp.core.State;

public interface StateHashFactory {

	public StateHashTuple hashState(State s);
	
}
