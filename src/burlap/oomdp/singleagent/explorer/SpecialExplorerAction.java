package burlap.oomdp.singleagent.explorer;

import burlap.oomdp.core.State;

public interface SpecialExplorerAction {
	public State applySpecialAction(State curState);
}
