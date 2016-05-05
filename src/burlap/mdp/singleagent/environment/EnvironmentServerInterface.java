package burlap.mdp.singleagent.environment;

import java.util.List;

/**
 * @author James MacGlashan.
 */
public interface EnvironmentServerInterface extends Environment{


	/**
	 * Adds one or more {@link burlap.mdp.singleagent.environment.EnvironmentObserver}s
	 * @param observers and {@link burlap.mdp.singleagent.environment.EnvironmentObserver}
	 */
	void addObservers(EnvironmentObserver...observers);

	/**
	 * Clears all {@link burlap.mdp.singleagent.environment.EnvironmentObserver}s from this server.
	 */
	void clearAllObservers();

	/**
	 * Removes one or more {@link burlap.mdp.singleagent.environment.EnvironmentObserver}s from this server.
	 * @param observers the {@link burlap.mdp.singleagent.environment.EnvironmentObserver}s to remove.
	 */
	void removeObservers(EnvironmentObserver...observers);


	/**
	 * Returns all {@link burlap.mdp.singleagent.environment.EnvironmentObserver}s registered with this server.
	 * @return all {@link burlap.mdp.singleagent.environment.EnvironmentObserver}s registered with this server.
	 */
	List<EnvironmentObserver> getObservers();

}
