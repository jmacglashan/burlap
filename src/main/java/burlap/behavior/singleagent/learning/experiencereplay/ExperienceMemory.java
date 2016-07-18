package burlap.behavior.singleagent.learning.experiencereplay;

import burlap.mdp.singleagent.environment.EnvironmentOutcome;

import java.util.List;

/**
 * And interface for storing experiences, where an experience is an environment interaction defined by
 * and {@link EnvironmentOutcome}.
 * @author James MacGlashan.
 */
public interface ExperienceMemory {

	/**
	 * Adds an experience to the memory. Depending on the implementation, may implicitly cause the forgetting
	 * of other memories
	 * @param eo the experience to add, defined by an {@link EnvironmentOutcome}
	 */
	void addExperience(EnvironmentOutcome eo);

	/**
	 * Samples up to n experiences from this memory. Depending on implementation fewer than n experiences
	 * may be sampled if the memory has less than n defined.
	 * @param n the desired number of experiences to sample
	 * @return a list of of up to n experiences defiend with {@link EnvironmentOutcome} objects.
	 */
	List<EnvironmentOutcome> sampleExperiences(int n);

	/**
	 * Resets/clears this memory
	 */
	void resetMemory();

}
