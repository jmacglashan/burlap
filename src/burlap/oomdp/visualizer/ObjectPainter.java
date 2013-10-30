package burlap.oomdp.visualizer;

import java.awt.Graphics2D;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;


/**
 * And interface for defining painters that can render object instances to a graphics context.
 * @author James MacGlashan
 *
 */
public interface ObjectPainter {

	/**
	 * Paints object instance ob to graphics context g2
	 * @param g2 graphics context to which the object should be painted
	 * @param s the state of the object to be painted
	 * @param ob the instantiated object to be painted
	 * @param cWidth width of the canvas size
	 * @param cHeight height of the canvas size
	 */
	public void paintObject(Graphics2D g2, State s, ObjectInstance ob, float cWidth, float cHeight);
	
	
}
