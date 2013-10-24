package burlap.behavior.singleagent.auxiliary.valuefunctionvis;

import java.awt.Graphics2D;

import burlap.behavior.singleagent.Policy;
import burlap.oomdp.core.State;

public interface StatePolicyPainter {

	/**
	 * @param g2 graphics context to which the object should be painted
	 * @param s the state of the object to be painted
	 * @param policy the policy that can be used on state s
	 * @param cWidth width of the canvas size
	 * @param cHeight height of the canvas size
	 */
	public void paintStateValue(Graphics2D g2, State s, Policy policy, float cWidth, float cHeight);
	
}
