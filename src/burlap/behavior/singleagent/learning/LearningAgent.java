package burlap.behavior.singleagent.learning;

import java.util.LinkedList;
import java.util.List;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.oomdp.core.State;

/**
 * This is the standard interface for defining an agent that learns how to behave in the world through experience. The primary method
 * is the {@link #runLearningEpisodeFrom(State)} method which causes the agent to interact with the world until it reaches a terminal state.
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










	/**
	 * Because {@link burlap.behavior.singleagent.learning.LearningAgent} is an interface, default methods for managing
	 * the history of experienced episodes is not provided. Therefore, this class provides the requisite data members
	 * and default methods for making that bookkeeping easy. This allows a class that implements the
	 * {@link burlap.behavior.singleagent.learning.LearningAgent} interface to simply have a data member for this
	 * class and allow it to manage the methods.
	 *
	 * All data members of this class are public. By default the history of learning episodes stored will be just
	 * 1. This method also includes a data member {@link #maxEpisodeSize} which can be optionally used for setting the longest
	 * time a learning agent will run when the {@link burlap.behavior.singleagent.learning.LearningAgent#runLearningEpisodeFrom(burlap.oomdp.core.State)}
	 * method is called. By default, it is INT MAX.
	 */
	public static class LearningAgentBookKeeping{

		/**
		 * The history of learning episodes
		 */
		public LinkedList<EpisodeAnalysis> 				episodeHistory = new LinkedList<EpisodeAnalysis>();
		public int										numEpisodesToStore = 1;
		protected int									maxEpisodeSize = Integer.MAX_VALUE;


		/**
		 * Returns the last learning episode of the agent.
		 * @return  the last learning episode of the agent.
		 */
		public EpisodeAnalysis getLastLearningEpisode(){
			return this.episodeHistory.getLast();
		}


		/**
		 * Tells the agent how many {@link burlap.behavior.singleagent.EpisodeAnalysis} objects representing learning episodes to internally store.
		 * For instance, if the number of set to 5, then the agent should remember the save the last 5 learning episodes. Note that this number
		 * has nothing to do with how learning is performed; it is purely for performance gathering.
		 * @param numEps the number of learning episodes to remember.
		 */
		public void setNumEpisodesToStore(int numEps){
			this.numEpisodesToStore = numEps;
		}

		/**
		 * Returns all saved {@link burlap.behavior.singleagent.EpisodeAnalysis} objects of which the agent has kept track.
		 * @return all saved {@link burlap.behavior.singleagent.EpisodeAnalysis} objects of which the agent has kept track.
		 */
		public List<EpisodeAnalysis> getAllStoredLearningEpisodes(){
			return this.episodeHistory;
		}


		/**
		 * Adds episode ea to this objects history of experienced episodes. If the current size of all episodes
		 * stored is equal to the number of episodes this object is supposed to store, it first removes the oldest
		 * episodes added before adding ea.
		 * @param ea the episode to add to the history of experienced episodes.
		 */
		public void offerEpisodeToHistory(EpisodeAnalysis ea){
			if(episodeHistory.size() >= numEpisodesToStore){
				episodeHistory.poll();
			}
			episodeHistory.offer(ea);
		}

	}
	
}
