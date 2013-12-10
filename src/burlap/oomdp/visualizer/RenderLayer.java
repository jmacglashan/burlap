package burlap.oomdp.visualizer;

import java.awt.Graphics2D;

/**
 * A RenderLayer is a 2 dimensional layer that paints to a provided 2D graphics context. The {@link MultiLayerRenderer} can take
 * a list of these objects and will paint them sequentially to the same 2D graphics context. This allows different kinds
 * of renderers that display different kinds of information to be layered on top of each other. 
 * @author James MacGlashan
 *
 */
public interface RenderLayer {
	public void render(Graphics2D g2, float width, float height);
}
