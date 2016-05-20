package burlap.mdp.singleagent.environment.extensions;

import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;

/**
 * A class that is told of interactions in an environment. This is typically called from an {@link EnvironmentServer}
 * which intercepts the environment interactions.
 * @author James MacGlashan.
 */
public interface EnvironmentObserver {

	/**
	 * This method is called when an {@link burlap.mdp.singleagent.environment.Environment} receives an action to execute, but before the
	 * {@link burlap.mdp.singleagent.environment.Environment} has completed execution.
	 * @param o the current {@link burlap.mdp.singleagent.environment.Environment} observation in which the the action begins execution.
	 * @param action the {@link burlap.mdp.core.Action} which will be executed in the {@link burlap.mdp.singleagent.environment.Environment}.
	 */
	void observeEnvironmentActionInitiation(State o, Action action);

	/**
	 * This method is called every time an {@link burlap.mdp.singleagent.environment.Environment} is interacted with.
	 * @param eo the resulting {@link burlap.mdp.singleagent.environment.EnvironmentOutcome}
	 */
	void observeEnvironmentInteraction(EnvironmentOutcome eo);

	/**
	 * This method is called every time an {@link burlap.mdp.singleagent.environment.Environment} is reset (has the {@link Environment#resetEnvironment()} method called).
	 * @param resetEnvironment the {@link burlap.mdp.singleagent.environment.Environment} that was reset.
	 */
	void observeEnvironmentReset(Environment resetEnvironment);
}
