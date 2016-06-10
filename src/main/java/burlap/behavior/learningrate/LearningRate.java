package burlap.behavior.learningrate;

import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;


/**
 * Provides an interface for different methods of learning rate decay schedules. Learning rates can be defined either by state-action pairs, or by state(-action) features.
 * The learnign rate can also be peeked at without affecting its value using the peek methods. Poll methods will cause the learning rate to decay. Note that
 * the poll methods also take as a parmater the agent time at which the learning rate is being polled. This is because it's possible for the agent to poll
 * the learning rate for multiple state-actions/features in any given time step, but if the learning rate function is indepdent of the states/features,
 * then it should only be decayed once for that time index.
 * @author James MacGlashan
 *
 */
public interface LearningRate {

	/**
	 * A method for looking at the current learning rate for a state-action pair without having it altered.
	 * @param s the state for which the learning rate should be returned
	 * @param ga the action from which the learning rate should be returned
	 * @return the current learning rate for the given state-action pair
	 */
	public double peekAtLearningRate(State s, Action ga);
	
	
	/**
	 * A method for returning the learning rate for a given state action pair and then decaying the learning rate as defined by this class.
	 * @param agentTime the time index of the agent when polling.
	 * @param s the state for which the learning rate should be returned
	 * @param ga the action from which the learning rate should be returned
	 * @return the current learning rate for the given state-action pair
	 */
	public double pollLearningRate(int agentTime, State s, Action ga);
	
	
	
	/**
	 * A method for looking at the current learning rate for a state (-action) feature without having it altered.
	 * @param featureId the state feature for which the learning rate should be returned
	 * @return the current learning rate for the given state feature-action pair
	 */
	public double peekAtLearningRate(int featureId);
	
	
	/**
	 * A method for returning the learning rate for a given state (-action) feature and then decaying the learning rate as defined by this class.
	 * @param agentTime the time index of the agent when polling.
	 * @param featureId the state feature for which the learning rate should be returned
	 * @return the current learning rate for the given state feature-action pair
	 */
	public double pollLearningRate(int agentTime, int featureId);
	
	
	
	
	/**
	 * Causes any learnign rate decay to reset to where it started.
	 */
	public void resetDecay();
	
	
}
