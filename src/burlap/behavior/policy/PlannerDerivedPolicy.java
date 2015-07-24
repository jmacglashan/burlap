package burlap.behavior.policy;

import burlap.behavior.singleagent.MDPSolver;

/**
 * An interface for defining policies that refer to a planner object to produce
 * the policy
 * @author James MacGlashan
 *
 */
public interface PlannerDerivedPolicy {
	/**
	 * Sets the planner whose results affect this policy.
	 * @param planner
	 */
	public void setPlanner(MDPSolver planner);
}
