package burlap.mdp.singleagent.model;

import burlap.mdp.singleagent.environment.EnvironmentOutcome;

/**
 * Tuple for the probability of a given transition in an MDP. The transition is stored using an {@link EnvironmentOutcome}
 * object, which specifies the previous state/observation of the environment, the action take, the next state/observation,
 * the reward received, and whether the transition led to a terminal state.
 * @author James MacGlashan.
 */
public class TransitionProb {

	/**
	 * The probability of the transition
	 */
	public double p;

	/**
	 * The transition
	 */
	public EnvironmentOutcome eo;


	/**
	 * Default constructor, primarily for serialization purposes.
	 */
	public TransitionProb() {
	}


	/**
	 * Initializes
	 * @param p the probability of the transition
	 * @param eo the transition
	 */
	public TransitionProb(double p, EnvironmentOutcome eo) {
		this.p = p;
		this.eo = eo;
	}
}
