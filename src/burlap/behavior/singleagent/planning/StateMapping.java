package burlap.behavior.singleagent.planning;

import burlap.oomdp.core.State;

public interface StateMapping {
	State mapState(State s);
}
