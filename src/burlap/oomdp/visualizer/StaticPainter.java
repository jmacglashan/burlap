/* Author: James MacGlashan
 * Description:
 * This class is defines the interface to which a painter that paints static properties of a
 * domain should adhere. That is, how properties of a domain that are not expressed in the
 * objects should be painted to the screen. Consider a Maze world with objects with which
 * the agents should interact. In this case the walls of the maze is not represented
 * by any of the instantiated objects, but should still be rendered to the screen
 * when visualizing this domain. A StaticPainter class should be sub classed that
 * defines how to do this
 */



package burlap.oomdp.visualizer;

import java.awt.Graphics2D;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;


public interface StaticPainter {
	
	
	/** 
	 * @param g2 graphics context to which the static data should be painted
	 * @param s the state to be painted
	 * @param cWidth the width of the canvas
	 * @param cHeight the height of the canvas
	 */
	public abstract void paint(Graphics2D g2, State s, float cWidth, float cHeight);

}
