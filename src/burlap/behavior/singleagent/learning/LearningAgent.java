package burlap.behavior.singleagent.learning;


import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.oomdp.singleagent.environment.Environment;

/**
 * This is the standard interface for defining an agent that learns how to behave in the world through experience. There
 * are two methods that need to be implemented. {@link #runLearningEpisode(burlap.oomdp.singleagent.environment.Environment)}
 * and {@link #runLearningEpisode(burlap.oomdp.singleagent.environment.Environment, int)}. Implementing the former method
 * should have the agent interact with the provided {@link burlap.oomdp.singleagent.environment.Environment}
 * until the {@link burlap.oomdp.singleagent.environment.Environment} transitions to a terminal state. The
 * {@link #runLearningEpisode(burlap.oomdp.singleagent.environment.Environment, int)} should have the agent interact
 * with the {@link burlap.oomdp.singleagent.environment.Environment} until either a terminal state is reached or
 * the agent has taken maxSteps in the environment. Both methods should return an {@link burlap.behavior.singleagent.EpisodeAnalysis}
 * object that records the interactions.
 *
 *
 * @author James MacGlashan
 *
 */
public interface LearningAgent {


	EpisodeAnalysis runLearningEpisode(Environment env);

	EpisodeAnalysis runLearningEpisode(Environment env, int maxSteps);

	
}
