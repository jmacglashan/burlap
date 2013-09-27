package burlap.behavior.singleagent.planning.deterministic.informed;

import burlap.oomdp.core.State;

public interface Heuristic {

	public double h(State s);
	
}
