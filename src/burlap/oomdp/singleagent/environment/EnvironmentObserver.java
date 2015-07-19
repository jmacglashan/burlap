package burlap.oomdp.singleagent.environment;

/**
 * A class that is told of interactions in an environment. This is typically called from an {@link burlap.oomdp.singleagent.environment.EnvironmentServer}
 * which intercepts the environment interactions.
 * @author James MacGlashan.
 */
public interface EnvironmentObserver {
	public void observeEnvironment(EnvironmentOutcome eo);
}
