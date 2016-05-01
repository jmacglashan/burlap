package burlap.oomdp.visualizer;

import burlap.oomdp.core.State;

import java.awt.*;



/**
 * This class paints general properties of a state/domain that may not be represented
 * by any specific object instance data. For instance, the GridWorld class
 * may have walls that need to be painted, but the walls are part of the transition
 * dynamics of the domain and not captured in the object instance values assignments.
 * @author James MacGlashan
 *
 */
public interface StatePainter {
	
	
	/** 
	 * Paints general state information not to graphics context g2
	 * @param g2 graphics context to which the static data should be painted
	 * @param s the state to be painted
	 * @param cWidth the width of the canvas
	 * @param cHeight the height of the canvas
	 */
	void paint(Graphics2D g2, State s, float cWidth, float cHeight);

}
