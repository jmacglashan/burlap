package burlap.mdp.singleagent;

import burlap.mdp.core.state.State;

public interface ActionObserver {
	public void actionEvent(State s, GroundedAction ga, State sp);
}
