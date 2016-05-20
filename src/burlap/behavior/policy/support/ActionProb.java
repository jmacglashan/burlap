package burlap.behavior.policy.support;

import burlap.mdp.core.Action;

/**
 * Class for storing an action and probability tuple. The probability represents the probability that the action will be selected.
 * @author James MacGlashan
 *
 */
public class ActionProb {

	/**
	 * The action to be considered.
	 */
	public Action ga;

	/**
	 * The probability of the action being selected.
	 */
	public double pSelection;

	public ActionProb() {
	}

	/**
	 * Initializes the action, probability tuple.
	 * @param ga the action to be considered
	 * @param p the probability of the action being selected.
	 */
	public ActionProb(Action ga, double p){
		this.ga = ga;
		this.pSelection = p;
	}

	@Override
	public String toString() {
		return this.pSelection + ": " + ga.toString();
	}
}
