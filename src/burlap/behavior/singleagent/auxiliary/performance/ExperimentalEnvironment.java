package burlap.behavior.singleagent.auxiliary.performance;

/**
 * An interface to be used in conjunction with {@link burlap.oomdp.singleagent.environment.Environment} implementations
 * that can accept a message informing the environment that a new experiment for a {@link burlap.behavior.singleagent.learning.LearningAgent} has started.
 * This is useful if when comparing multiple agents the same initial state sequence is desired.
 * @author James MacGlashan.
 */
public interface ExperimentalEnvironment {

	/**
	 * Tells this {@link burlap.oomdp.singleagent.environment.Environment} that an experiment with a new {@link burlap.behavior.singleagent.learning.LearningAgent}
	 * has begun.
	 */
	void startNewExperiment();
}
