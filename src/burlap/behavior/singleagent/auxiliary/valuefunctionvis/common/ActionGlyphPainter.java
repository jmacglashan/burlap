package burlap.behavior.singleagent.auxiliary.valuefunctionvis.common;

import java.awt.Graphics2D;


/**
 * An interface for painting glyphs that correspond to actions.
 * @author James MacGlashan
 *
 */
public interface ActionGlyphPainter {
	/**
	 * Called to paint a glyph in the rectangle defined by the top left origin (x,y) with the given width and height.
	 * @param g2 the graphics context to paint to
	 * @param x the left of the rectangle origin
	 * @param y the top of the rectangle origin
	 * @param width the width of the rectangle
	 * @param height the height of the rectangle.
	 */
	public void paintGlyph(Graphics2D g2, float x, float y, float width, float height);
}
