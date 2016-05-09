package burlap.mdp.singleagent.pomdp.observations;

import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.GroundedAction;


/**
 * Defines the observation function and observations associated with a POMDP domain ({@link burlap.mdp.singleagent.pomdp.PODomain}).
 * Requires two methods: {@link #probability(State, State, GroundedAction)} and {@link #sample(State, GroundedAction)}.
 * The former defines the probability mass/density function for an observation given a hidden state and action that
 * led the hidden state. The latter samples and observation given a hidden state and action that led to the hidden state.
 */
public interface ObservationFunction {


	/**
	 * Returns the probability that an observation will be observed conditioned on the MDP state and previous action taken that led to the state.
	 * @param observation the observation, represented by a {@link State}
	 * @param state The true MDP state that generated the observation.
	 * @param action the action that led to the MDP state and which generated the observation
	 * @return the probability of observing the observation.
	 */
	double probability(State observation, State state, GroundedAction action);



	/**
	 * Samples an observation given the true MDP state and action taken in the previous step that led to the MDP state.
	 * @param state the true MDP state
	 * @param action the action that led to the MDP state
	 * @return an observation represented with a {@link State}.
	 */
	State sample(State state, GroundedAction action);






}
