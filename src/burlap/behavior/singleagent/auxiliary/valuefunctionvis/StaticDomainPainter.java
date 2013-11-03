package burlap.behavior.singleagent.auxiliary.valuefunctionvis;

import java.awt.Graphics2D;


/**
 * An interface for painting general domain information to a 2D graphics context.
 * @author James MacGlashan
 *
 */
public interface StaticDomainPainter {

	/** 
	 * Use to paint general domain information to a 2D graphics context.
	 * @param g2 graphics context to which the static data should be painted
	 * @param cWidth the width of the canvas
	 * @param cHeight the height of the canvas
	 */
	public void paint(Graphics2D g2, float cWidth, float cHeight);
	
}
