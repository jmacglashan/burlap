package burlap.mdp.singleagent.pomdp.observations;

import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;

import java.util.List;

/**
 * Defines additional methods for an {@link ObservationFunction} for the case when the set of observations are
 * discrete and able to be enumerated. Requires implementing the {@link #allObservations()} method and
 * {@link #probabilities(State, Action)} method. The former should return all possible observations; the
 * latter should return the set of observations with non-zero probability and their probabilities, for a given
 * input hidden state and action that led to the hidden state.
 * @author James MacGlashan.
 */
public interface DiscreteObservationFunction extends ObservationFunction {

	/**
	 * Returns a {@link java.util.List} containing all possible observations. Observations are represented with a class that implements the {@link State}
	 * interface, since observations may consist of multiple observations of distinct objects in the world.
	 * @return a {@link java.util.List} of all possible observations.
	 */
	List<State> allObservations();

	/**
	 * Returns the observation probability mass/density function for all observations that have non-zero mass/density conditioned on the true MDP state and previous action taken that led to the state.
	 * The function is represented as a {@link java.util.List} of {@link ObservationProbability} objects,
	 * which is a pair of an observation (represented by a {@link State} and double specifying its mass/density.
	 * @param state the true MDP state that generated the observations
	 * @param action the action that led to the MDP state and which generated the observations.
	 * @return the observation probability mass/density function represented by a {@link java.util.List} of {@link ObservationProbability} objects.
	 */
	List<ObservationProbability> probabilities(State state, Action action);

}
