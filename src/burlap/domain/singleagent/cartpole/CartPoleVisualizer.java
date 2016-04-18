package burlap.domain.singleagent.cartpole;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.visualizer.ObjectPainter;
import burlap.oomdp.visualizer.StateRenderLayer;
import burlap.oomdp.visualizer.Visualizer;


/**
 * Class for returning cart pole visualizer objects.
 * @author James MacGlashan
 *
 */
public class CartPoleVisualizer {

    private CartPoleVisualizer() {
        // do nothing
    }
    
	/**
	 * Returns a visualizer for cart pole.
	 * @return a visualizer for cart pole.
	 */
	public static Visualizer getCartPoleVisualizer(){
		return new Visualizer(getCartPoleStateRenderLayer());
	}
	
	
	/**
	 * Returns a StateRenderLayer for cart pole.
	 * @return a StateRenderLayer for cart pole.
	 */
	public static StateRenderLayer getCartPoleStateRenderLayer(){
		StateRenderLayer rl = new StateRenderLayer();
		rl.addObjectClassPainter(CartPoleDomain.CLASSCARTPOLE, new CartPoleObjectPainter());
		return rl;
	}
	
	
	/**
	 * An object painter for the cart pole object. Cart will have width/height of 0.1 * the canvas width and pole will have
	 * length of 0.5 * the canvas width.
	 * @author James MacGlashan
	 *
	 */
	public static class CartPoleObjectPainter implements ObjectPainter{

		@Override
		public void paintObject(Graphics2D g2, State s, ObjectInstance ob,
				float cWidth, float cHeight) {
			
			
			double x = ob.getRealValForAttribute(CartPoleDomain.ATTX);
			double a = ob.getRealValForAttribute(CartPoleDomain.ATTANGLE);
			
			Attribute xatt = ob.getObjectClass().getAttribute(CartPoleDomain.ATTX);
			
			double xmin = xatt.lowerLim;
			double xmax = xatt.upperLim;
			double xrange = xmax-xmin;
			
			
			//manage cart rendering
			float nx = (float)((x-xmin)/xrange);
			
			float cartRadius = 0.05f * cWidth;
			
			float scx = nx*cWidth;
			float scy = cHeight - cartRadius;
			
			
			g2.setColor(Color.black);
			g2.fill(new Rectangle2D.Float(scx-cartRadius, scy-cartRadius, 2*cartRadius, 2*cartRadius));
			
			
			//manage pole rendering
			float poleLength = 0.5f * cHeight;
			float poleTipX = poleLength * (float)Math.sin(a) + scx;
			float poleTipY = -poleLength * (float)Math.cos(a) + (scy - cartRadius);
			
			g2.setColor(Color.gray);
			g2.setStroke(new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
			g2.draw(new Line2D.Float(scx, scy - cartRadius, poleTipX, poleTipY));
			
			
		}
		
	
	}
}
