package burlap.oomdp.stochasticgames;

import burlap.oomdp.core.State;

import java.util.Map;

/**
 * An interface for defining {@link burlap.oomdp.stochasticgames.World} observers. Observers
 * are told when a game states and in what state, what each interaction in the world was as they happen, and
 * what the final state of the world is when a game ends.
 */
public interface WorldObserver {

	/**
	 * This method is called whenever a new game in a world is starting.
	 * @param s the state in which the world is starting.
	 */
	void gameStarting(State s);

	/**
	 * This method is called whenever an interaction in the world occurs.
	 * @param s the previous state of the world
	 * @param ja the joint action taken in the world
	 * @param reward the joint reward received by the agents
	 * @param sp the next state of the world
	 */
	void observe(State s, JointAction ja, Map<String, Double> reward, State sp);

	/**
	 * This method is called whenever a game in a world ends.
	 * @param s the final state of the world when it ends.
	 */
	void gameEnding(State s);
}
