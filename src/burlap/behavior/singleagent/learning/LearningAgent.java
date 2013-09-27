package burlap.behavior.singleagent.learning;

import java.util.List;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.oomdp.core.State;


public interface LearningAgent {

	public EpisodeAnalysis runLearningEpisodeFrom(State initialState);
	
	public EpisodeAnalysis getLastLearningEpisode();
	public void setNumEpisodesToStore(int numEps);
	public List<EpisodeAnalysis> getAllStoredLearningEpisodes();
	
}
