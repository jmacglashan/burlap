package burlap.behavior.singleagent.vfa.rbf;

import burlap.oomdp.core.State;

/**
 * An interface for defining distant metrics between OO-MDP {@link State} objects.
 * @author Anubhav Malhotra and Daniel Fernandez and Spandan Dutta
 *
 */
public interface DistanceMetric {
	public double distance(State s0, State s1);
}
