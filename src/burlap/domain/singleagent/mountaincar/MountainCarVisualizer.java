package burlap.domain.singleagent.mountaincar;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.visualizer.ObjectPainter;
import burlap.oomdp.visualizer.StateRenderLayer;
import burlap.oomdp.visualizer.StaticPainter;
import burlap.oomdp.visualizer.Visualizer;


/**
 * A class for creating a {@link burlap.oomdp.visualizer.Visualizer} for a {@link burlap.domain.singleagent.mountaincar.MountainCar} {@link burlap.oomdp.core.Domain}.
 * The agent will be drawn as a red square and the shape of the hill in a black line.
 */
public class MountainCarVisualizer {

	
	/**
	 * Returns a {@link burlap.oomdp.visualizer.Visualizer} for a {@link burlap.domain.singleagent.mountaincar.MountainCar} {@link burlap.oomdp.core.Domain}
	 * using the hill design/physics defined in the {@link burlap.oomdp.auxiliary.DomainGenerator} for visualization
	 * @param mcGen the generator for a given mountain car domain that is to be visualized.
	 * @return a {@link burlap.oomdp.visualizer.Visualizer} for the mountain car domain.
	 */
	public static Visualizer getVisualizer(MountainCar mcGen){
		
		return new Visualizer(getStateRenderLayer(mcGen.physParams));
		
	}


	/**
	 * Returns a {@link burlap.oomdp.visualizer.Visualizer} for a {@link burlap.domain.singleagent.mountaincar.MountainCar} {@link burlap.oomdp.core.Domain}
	 * using the hill design/physics defined in the {@link burlap.domain.singleagent.mountaincar.MountainCar.MCPhysicsParams} for visualization
	 * @param physParams the physics/hill design to be visualized
	 * @return a {@link burlap.oomdp.visualizer.Visualizer} for a {@link burlap.domain.singleagent.mountaincar.MountainCar} {@link burlap.oomdp.core.Domain}
	 */
	public static Visualizer getVisualizer(MountainCar.MCPhysicsParams physParams){
		return new Visualizer(getStateRenderLayer(physParams));
	}


	/**
	 * Returns a {@link burlap.oomdp.visualizer.StateRenderLayer} for a {@link burlap.domain.singleagent.mountaincar.MountainCar} {@link burlap.oomdp.core.Domain}
	 * using the hill design/physics defined in the {@link burlap.domain.singleagent.mountaincar.MountainCar.MCPhysicsParams} for visualization
	 * @param physParams the physics/hill design to be visualized
	 * @return a {@link burlap.oomdp.visualizer.StateRenderLayer} for a {@link burlap.domain.singleagent.mountaincar.MountainCar} {@link burlap.oomdp.core.Domain}
	 */
	public static StateRenderLayer getStateRenderLayer(MountainCar.MCPhysicsParams physParams){

		StateRenderLayer slr = new StateRenderLayer();
		slr.addStaticPainter(new HillPainter(physParams));
		slr.addObjectClassPainter(MountainCar.CLASSAGENT, new AgentPainter(physParams));

		return slr;

	}
	
	
	
	/**
	 * Class for paining the agent in the mountain car domain. The car will be rendered as a red square.
	 * @author James MacGlashan
	 *
	 */
	public static class AgentPainter implements ObjectPainter{

		MountainCar.MCPhysicsParams physParams;
		
		
		/**
		 * Initializes with the mountain car physics used
		 * @param physParams the mountain car physics used
		 */
		public AgentPainter(MountainCar.MCPhysicsParams physParams){
			this.physParams = physParams;
		}
		
		
		@Override
		public void paintObject(Graphics2D g2, State s, ObjectInstance ob,
				float cWidth, float cHeight) {
			
			double worldWidth = physParams.xmax - physParams.xmin;
			
			double renderAgentWidth = 0.04*cWidth;
			
			double ox = ob.getRealValForAttribute(MountainCar.ATTX);
			double oy = Math.sin(this.physParams.cosScale*ox);
			
			double nx = (ox - this.physParams.xmin) / worldWidth;
			double ny = (oy + 1) / 2;
			
			double sx = (nx * cWidth) - (renderAgentWidth / 2);
			double sy = cHeight - (ny * (cHeight-30)+15) - (renderAgentWidth / 2);
			
			
			g2.setColor(Color.red);
			
			g2.fill(new Rectangle2D.Double(sx, sy, renderAgentWidth, renderAgentWidth));
			
		}
		
		
	}
	
	
	
	/**
	 * Class for drawing a black outline of the hill that the mountain car climbs.
	 * @author James MacGlashan
	 *
	 */
	public static class HillPainter implements StaticPainter{

		MountainCar.MCPhysicsParams physParams;
		
		
		/**
		 * Initializes with the mountain car physics used
		 * @param physParams the mountain car physics used
		 */
		public HillPainter(MountainCar.MCPhysicsParams physParams){
			this.physParams = physParams;
		}
		
		@Override
		public void paint(Graphics2D g2, State s, float cWidth, float cHeight) {
			
			g2.setColor(Color.black);
			g2.setStroke(new BasicStroke(3F));
			
			//create collection of sin points in world space
			int n = 1000;
			double range = this.physParams.xmax-this.physParams.xmin;
			double inc = (range)/n;
			List <MyPoint> worldPoints = new ArrayList<MyPoint>(n);
			for(int i = 0; i < n; i++){
				double x = this.physParams.xmin + (i * inc);
				double y = Math.sin(this.physParams.cosScale*x);
				worldPoints.add(new MyPoint(x, y));
				
			}
			
			for(int i = 0; i < n-1; i++){
				MyPoint p0 = worldPoints.get(i);
				MyPoint p1 = worldPoints.get(i+1);
				
				//draw it
				double nx0 = (p0.x - this.physParams.xmin) / (range);
				double ny0 = (p0.y + 1) / 2;
				
				double nx1 = (p1.x - this.physParams.xmin) / (range);
				double ny1 = (p1.y + 1) / 2;
				
				double sx0 = (nx0 * cWidth);
				double sy0 = (cHeight) - (ny0 * (cHeight-30)+15);
				
				double sx1 = (nx1 * cWidth);
				double sy1 = (cHeight) - (ny1 * (cHeight-30)+15);
				
				g2.draw(new Line2D.Double(sx0, sy0, sx1, sy1));
				
				
			}
			
		}
		
		
		/**
		 * Class for storing an x-y tuple.
		 * @author James MacGlashan
		 *
		 */
		private class MyPoint{
			public double x;
			public double y;
			public MyPoint(double x, double y){
				this.x = x;
				this.y = y;
			}
		}


		
		
		
		
		
	}
	
}
