package burlap.oomdp.singleagent.explorer;

import burlap.oomdp.core.State;

public class StateResetSpecialAction implements SpecialExplorerAction {

	State baseState;
	
	public StateResetSpecialAction(State s){
		baseState = s;
	}
	
	public void setBase(State s){
		baseState = s;
	}
	
	@Override
	public State applySpecialAction(State curState) {
		return baseState;
	}

}
