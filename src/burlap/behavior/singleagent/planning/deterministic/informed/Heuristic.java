package burlap.behavior.singleagent.planning.deterministic.informed;

import burlap.oomdp.core.State;

/**
 * An interface for defining heuristics. The heuristic function should return an estimate of the amount of *reward* that will be accumulated from that given
 * state. Since deterministic forward search planning algorithms typically expect costs, this is represented by simply using negative reward, where
 * values closer to zero are better. For instance, if it was known that a state was 3 steps away from the goal, an optimal heuristic (and the true cost
 * from the state) would return -3.
 * @author James MacGlashan
 *
 */
public interface Heuristic {

	/**
	 * Returns the estimated amount of reward that will be received when following the optimal policy from the given state.
	 * Since deterministic forward search planning algorithms typically expect costs, this is represented by simply using negative reward, where
	 * values closer to zero are better. For instance, if it was known that state s was 3 steps away from the goal, an optimal heuristic (the true reward
	 * from the state) would return -3.
	 * @param s the state from which to estimate the future reward.
	 * @return  the estimated amount of reward that will be received when following the optimal policy from s.
	 */
	public double h(State s);
	
}
