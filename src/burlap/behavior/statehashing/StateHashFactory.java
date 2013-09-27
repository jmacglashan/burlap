package burlap.behavior.statehashing;

import burlap.oomdp.core.State;

public class StateHashFactory {

	public StateHashTuple hashState(State s){
		return new StateHashTuple(s);
	}
	
}
