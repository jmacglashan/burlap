package burlap.behavior.singleagent.planning;

import burlap.behavior.policy.Policy;

/**
 * Represents an Observer which is called with the evolving policy as a Planning algorithm carries out
 * the construction of the policy (permitting an evaluation of how the policy is evolving.)
 */
public interface PlanningObserver {

    void observe(Policy p, int iteration, double delta);
}
