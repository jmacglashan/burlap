package burlap.domain.singleagent.cartpole;

import burlap.domain.singleagent.cartpole.states.CartPoleState;
import burlap.domain.singleagent.cartpole.states.InvertedPendulumState;
import burlap.mdp.core.state.State;
import burlap.mdp.visualizer.StatePainter;
import burlap.mdp.visualizer.StateRenderLayer;
import burlap.mdp.visualizer.Visualizer;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;


/**
 * Class for returning cart pole visualizer objects. Works for all instances of {@link InvertedPendulumState}.
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
		rl.addStatePainter(new CartPolePainter());
		return rl;
	}
	
	
	/**
	 * An object painter for the cart pole object. Cart will have width/height of 0.1 * the canvas width and pole will have
	 * length of 0.5 * the canvas width.
	 * @author James MacGlashan
	 *
	 */
	public static class CartPolePainter implements StatePainter{


		@Override
		public void paint(Graphics2D g2, State s,
				float cWidth, float cHeight) {


			InvertedPendulumState is = (InvertedPendulumState)s;
			double x = is instanceof CartPoleState ? ((CartPoleState)is).x : 0.;

			double a = is.angle;

			
			double xmin = -2.4;
			double xmax = 2.4;
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
