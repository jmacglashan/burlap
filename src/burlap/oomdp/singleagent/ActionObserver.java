package burlap.oomdp.singleagent;

import burlap.oomdp.core.State;

public interface ActionObserver {
	public void actionEvent(State s, GroundedAction ga, State sp);
}
