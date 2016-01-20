package burlap.behavior.singleagent.learning;

import java.util.List;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.oomdp.core.State;

/**
 * This is the standard interface for defining an agent that learns how to behave in the world through experience. The primary method
 * is the {@link runLearningEpisodeFrom(State)} method which causes the agent to interact with the world until it reaches a terminal state.
 * The interface also provides some common mechanisms for getting the last learning episode the agent performed, storing a history
 * of learning episodes, and returning the history of stored episodes. 
 * @author James MacGlashan
 *
 */
public interface LearningAgent {

	/**
	 * Causes the agent to perform a learning episode starting in the given initial state. The episode terminates when a terminal
	 * state is reached or if the agent decides to determinate the episode (e.g., by having an internal parameter set for a
	 * maximum number of steps in an episode).
	 * @param initialState The initial state in which the agent will start the episode.
	 * @return The learning episode events that was performed, stored in an {@link burlap.behavior.singleagent.EpisodeAnalysis} object.
	 */
	public EpisodeAnalysis runLearningEpisodeFrom(State initialState);
	
	
	/**
	 * Causes the agent to perform a learning episode starting in the given initial state. The episode terminates when a terminal
	 * state is reached, if the agent decides to determinate the episode, or if the number of steps reaches the provided threshold.
	 * @param initialState The initial state in which the agent will start the episode.
	 * @param maxSteps the maximum number of steps in the episode
	 * @return The learning episode events that was performed, stored in an {@link burlap.behavior.singleagent.EpisodeAnalysis} object.
	 */
	public EpisodeAnalysis runLearningEpisodeFrom(State initialState, int maxSteps);
	
	/**
	 * Returns the last learning episode of the agent.
	 * @return  the last learning episode of the agent.
	 */
	public EpisodeAnalysis getLastLearningEpisode();
	
	/**
	 * Tells the agent how many {@link burlap.behavior.singleagent.EpisodeAnalysis} objects representing learning episodes to internally store.
	 * For instance, if the number of set to 5, then the agent should remember the save the last 5 learning episodes. Note that this number
	 * has nothing to do with how learning is performed; it is purely for performance gathering.
	 * @param numEps the number of learning episodes to remember.
	 */
	public void setNumEpisodesToStore(int numEps);
	
	/**
	 * Returns all saved {@link burlap.behavior.singleagent.EpisodeAnalysis} objects of which the agent has kept track.
	 * @return all saved {@link burlap.behavior.singleagent.EpisodeAnalysis} objects of which the agent has kept track.
	 */
	public List<EpisodeAnalysis> getAllStoredLearningEpisodes();
	
}
