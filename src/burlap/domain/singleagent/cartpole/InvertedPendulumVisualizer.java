package burlap.domain.singleagent.cartpole;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import burlap.oomdp.core.objects.OldObjectInstance;
import burlap.oomdp.core.state.State;
import burlap.oomdp.visualizer.ObjectPainter;
import burlap.oomdp.visualizer.StateRenderLayer;
import burlap.oomdp.visualizer.Visualizer;

public class InvertedPendulumVisualizer {

    private InvertedPendulumVisualizer() {
        
    }
    
	
	/**
	 * Returns a {@link Visualizer} object for the {@link InvertedPendulum} domain.
	 * @return a {@link Visualizer} object for the {@link InvertedPendulum} domain.
	 */
	public static Visualizer getInvertedPendulumVisualizer(){
		return new Visualizer(getInvertedPendulumStateRenderLayer());
	}
	
	/**
	 * Returns a {@link StateRenderLayer} object for the {@link InvertedPendulum} domain.
	 * @return a {@link StateRenderLayer} object for the {@link InvertedPendulum} domain.
	 */
	public static StateRenderLayer getInvertedPendulumStateRenderLayer(){
		StateRenderLayer sl = new StateRenderLayer();
		sl.addObjectClassPainter(InvertedPendulum.CLASSPENDULUM, new PendulumObjectPainter());
		return sl;	
	}
	
	/**
	 * An object painter for the pendulum object. Fixed cart will have width/height of 0.1 * the canvas width and pole will have
	 * length of 0.5 * the canvas width.
	 * @author James MacGlashan
	 *
	 */
	public static class PendulumObjectPainter implements ObjectPainter{

		@Override
		public void paintObject(Graphics2D g2, State s, OldObjectInstance ob,
			float cWidth, float cHeight) {
			
			
			double a = ob.getRealValForAttribute(InvertedPendulum.ATTANGLE);
			
			
			
			//manage cart rendering
			float nx = 0.5f;
			
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
