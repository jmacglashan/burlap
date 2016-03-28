package burlap.behavior.singleagent.learning.experiencereplay;

import burlap.oomdp.singleagent.environment.EnvironmentOutcome;

import java.util.List;

/**
 * @author James MacGlashan.
 */
public interface ExperiencesMemory {
	void addExperience(EnvironmentOutcome eo);
	List<EnvironmentOutcome> sampleExperiences(int n);
	void resetMemory();
}
