package burlap.behavior.singleagent.planning.vfa.fittedvi;

import burlap.oomdp.core.State;

/**
 * An interface
 * @author James MacGlashan.
 */
public interface BlackboxVFA {
	public double value(State s);
}
