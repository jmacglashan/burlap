package burlap.behavior.singleagent.planning;

import java.util.ArrayList;

import burlap.domain.singleagent.minecraft.Affordance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;

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
	public void setPlanner(OOMDPPlanner planner);

	public GroundedAction getAffordanceAction(State s, ArrayList<Affordance> kb);
}
