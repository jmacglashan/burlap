package burlap.mdp.singleagent.environment.extensions;

import burlap.mdp.singleagent.environment.Environment;

import java.util.List;

/**
 * An interface for {@link Environment} implementations that also can serve {@link EnvironmentObserver} instances
 * about the interactions in the {@link Environment}.
 * @author James MacGlashan.
 */
public interface EnvironmentServerInterface extends Environment{


	/**
	 * Adds one or more {@link EnvironmentObserver}s
	 * @param observers and {@link EnvironmentObserver}
	 */
	void addObservers(EnvironmentObserver...observers);

	/**
	 * Clears all {@link EnvironmentObserver}s from this server.
	 */
	void clearAllObservers();

	/**
	 * Removes one or more {@link EnvironmentObserver}s from this server.
	 * @param observers the {@link EnvironmentObserver}s to remove.
	 */
	void removeObservers(EnvironmentObserver...observers);


	/**
	 * Returns all {@link EnvironmentObserver}s registered with this server.
	 * @return all {@link EnvironmentObserver}s registered with this server.
	 */
	List<EnvironmentObserver> observers();

}
