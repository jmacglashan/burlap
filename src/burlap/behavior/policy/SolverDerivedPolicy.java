package burlap.behavior.policy;

import burlap.behavior.singleagent.MDPSolverInterface;


/**
 * An interface for defining policies that refer to a {@link burlap.behavior.singleagent.MDPSolverInterface}
 * objects to defined the policy. For example, selecting actions based on the maximum Q-value that a solver computed.
 * @author James MacGlashan
 *
 */
public interface SolverDerivedPolicy {
	/**
	 * Sets the valueFunction whose results affect this policy.
	 * @param solver the solver from which this policy is derived
	 */
	public void setSolver(MDPSolverInterface solver);
}
