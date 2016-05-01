package burlap.oomdp.singleagent.environment;

import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;

/**
 * A class that is told of interactions in an environment. This is typically called from an {@link burlap.oomdp.singleagent.environment.EnvironmentServer}
 * which intercepts the environment interactions.
 * @author James MacGlashan.
 */
public interface EnvironmentObserver {

	/**
	 * This method is called when an {@link burlap.oomdp.singleagent.environment.Environment} receives an action to execute, but before the
	 * {@link burlap.oomdp.singleagent.environment.Environment} has completed execution.
	 * @param o the current {@link burlap.oomdp.singleagent.environment.Environment} observation in which the the action begins execution.
	 * @param action the {@link burlap.oomdp.singleagent.GroundedAction} which will be executed in the {@link burlap.oomdp.singleagent.environment.Environment}.
	 */
	void observeEnvironmentActionInitiation(State o, GroundedAction action);

	/**
	 * This method is called every time an {@link burlap.oomdp.singleagent.environment.Environment} is interacted with.
	 * @param eo the resulting {@link burlap.oomdp.singleagent.environment.EnvironmentOutcome}
	 */
	void observeEnvironmentInteraction(EnvironmentOutcome eo);

	/**
	 * This method is called every time an {@link burlap.oomdp.singleagent.environment.Environment} is reset (has the {@link Environment#resetEnvironment()} method called).
	 * @param resetEnvironment the {@link burlap.oomdp.singleagent.environment.Environment} that was reset.
	 */
	void observeEnvironmentReset(Environment resetEnvironment);
}
