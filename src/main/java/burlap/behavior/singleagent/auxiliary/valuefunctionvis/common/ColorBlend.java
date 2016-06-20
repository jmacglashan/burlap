package burlap.behavior.singleagent.auxiliary.valuefunctionvis.common;

import java.awt.Color;

/**
 * An interface for defining methods that return a color for a given double value.
 * @author James MacGlashan
 *
 */
public interface ColorBlend {
	
	/**
	 * Returns a {@link java.awt.Color} for a given double value
	 * @param v the input double value
	 * @return a {@link java.awt.Color} for a given double value
	 */
	public Color color(double v);
	
	/**
	 * Tells this object the minimum value and the maximum value it can receive.
	 * @param minV the minimum value
	 * @param maxV the maximum value
	 */
	public void rescale(double minV, double maxV);
}
