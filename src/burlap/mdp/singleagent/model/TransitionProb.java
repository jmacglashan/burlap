package burlap.mdp.singleagent.model;

import burlap.mdp.singleagent.environment.EnvironmentOutcome;

/**
 * @author James MacGlashan.
 */
public class TransitionProb {
	public double p;
	public EnvironmentOutcome eo;

	public TransitionProb() {
	}

	public TransitionProb(double p, EnvironmentOutcome eo) {
		this.p = p;
		this.eo = eo;
	}
}
