package burlap.behavior.stochasticgame.agents.learningrate;

import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.GroundedSingleAction;


/**
 * Provides an interface for different methods of learning rate decay for stochastic games.
 * @author James MacGlashan
 *
 */
public interface SGLearningRate {

	/**
	 * A method for looking at the current learning rate for a state-action pair without having it altered.
	 * @param s the state for which the learning rate should be returned
	 * @param ga the action from which the learning rate should be returned
	 * @return the current learning rate for the given state-action pair
	 */
	public double peekAtLearningRate(State s, GroundedSingleAction ga);
	
	
	/**
	 * A method for returning the learning rate for a given state action pair and then decaying the learning rate as defined by this class.
	 * @param s the state for which the learning rate should be returned
	 * @param ga the action from which the learning rate should be returned
	 * @return the current learning rate for the given state-action pair
	 */
	public double pollLearningRate(State s, GroundedSingleAction ga);
	
}
