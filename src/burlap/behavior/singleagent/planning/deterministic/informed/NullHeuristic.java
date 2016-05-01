package burlap.behavior.singleagent.planning.deterministic.informed;

import burlap.oomdp.core.state.State;

/**
 * A {@link Heuristic} implementation that always returns 0. This is always admissible
 * and effectively causes planners like A* to perform Uniform Cost Search.
 * @author James MacGlashan
 *
 */
public class NullHeuristic implements Heuristic {

	@Override
	public double h(State s) {
		return 0;
	}

}
