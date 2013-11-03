package burlap.behavior.singleagent.shaping.potential;

import burlap.oomdp.core.State;


/**
 * Defines an interface for reward potential functions. This interface will be used by potential-based reward shaping.
 * @author James MacGlashan
 *
 */
public interface PotentialFunction {
	
	/**
	 * Returns the reward potential from the given state.
	 * @param s the input state for which to get the reward potential.
	 * @return the reward potential from the given state.
	 */
	public double potentialValue(State s);
}
