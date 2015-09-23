package burlap.behavior.singleagent.auxiliary.valuefunctionvis;

import java.awt.Graphics2D;

import burlap.behavior.policy.Policy;
import burlap.oomdp.core.states.State;

/**
 * An interface for painting a representation of the policy for a specific state onto a 2D Graphics context.
 * @author James MacGlashan
 *
 */
public interface StatePolicyPainter {

	/**
	 * Paints a representation of the given policy for a specific state to a 2D graphics context.
	 * @param g2 graphics context to which the object should be painted
	 * @param s the state of the object to be painted
	 * @param policy the policy that can be used on state s
	 * @param cWidth width of the canvas size
	 * @param cHeight height of the canvas size
	 */
	public void paintStatePolicy(Graphics2D g2, State s, Policy policy, float cWidth, float cHeight);
	
}
