package burlap.mdp.singleagent.pomdp.observations;

import burlap.mdp.core.state.State;

/**
 * A class for associating a probability with an observation. The class is pair consisting of a {@link State}
 * for the observation and a double for the probability.
 */
public class ObservationProbability {

	/**
	 * The observation represented with a {@link State} object.
	 */
	public State observation;

	/**
	 * The probability of the observation
	 */
	public double p;


	/**
	 * Initializes.
	 * @param observation the observation represented with a {@link State} object.
	 * @param p the probability of the observation
	 */
	public ObservationProbability(State observation, double p){
		this.observation = observation;
		this.p = p;
	}
}
