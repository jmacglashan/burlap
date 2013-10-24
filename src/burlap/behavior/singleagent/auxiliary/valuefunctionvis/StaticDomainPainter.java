package burlap.behavior.singleagent.auxiliary.valuefunctionvis;

import java.awt.Graphics2D;

public interface StaticDomainPainter {

	/** 
	 * @param g2 graphics context to which the static data should be painted
	 * @param cWidth the width of the canvas
	 * @param cHeight the height of the canvas
	 */
	public void paint(Graphics2D g2, float cWidth, float cHeight);
	
}
