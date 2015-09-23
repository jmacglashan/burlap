package burlap.oomdp.singleagent.environment;

/**
 * A class that is told of interactions in an environment. This is typically called from an {@link burlap.oomdp.singleagent.environment.EnvironmentServer}
 * which intercepts the environment interactions.
 * @author James MacGlashan.
 */
public interface EnvironmentObserver {

	/**
	 * This method is called every time an {@link burlap.oomdp.singleagent.environment.Environment} is interacted with.
	 * @param eo the resulting {@link burlap.oomdp.singleagent.environment.EnvironmentOutcome}
	 */
	public void observeEnvironmentInteraction(EnvironmentOutcome eo);

	/**
	 * This method is called every time an {@link burlap.oomdp.singleagent.environment.Environment} is reset (has the {@link Environment#resetEnvironment()} method called).
	 * @param resetEnvironment the {@link burlap.oomdp.singleagent.environment.Environment} that was reset.
	 */
	public void observeEnvironmentReset(Environment resetEnvironment);
}
