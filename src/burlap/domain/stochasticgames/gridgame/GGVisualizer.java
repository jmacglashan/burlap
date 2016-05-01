package burlap.domain.stochasticgames.gridgame;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.objects.OldObjectInstance;
import burlap.oomdp.core.state.State;
import burlap.oomdp.visualizer.ObjectPainter;
import burlap.oomdp.visualizer.Visualizer;


/**
 * A class for visualizing the grid games. Agents are visualized as circles and goal locations
 * are visualized as square cells. Different agents are rendered different colors. Personal
 * goals for each agent are the same color as the agent for whom the goal corresponds, except
 * at a slightly darker hue. Universal goals are colored grey. Walls are drawn either as
 * solid lines for solid walls, or as dashed lines for semi-walls.
 * @author James MacGlashan
 *
 */
public class GGVisualizer {
    
    private GGVisualizer() {
        // do nothing
    }

	
	/**
	 * Generates a visualizer for a grid game
	 * @param maxX the width of the playing field
	 * @param maxY the height of the playing field
	 * @return a visualizer for the grid game
	 */
	public static Visualizer getVisualizer(int maxX, int maxY){
		
		Visualizer v = new Visualizer();
		
		
		List <Color> agentColors = new ArrayList<Color>();
		agentColors.add(Color.green);
		agentColors.add(Color.blue);
		agentColors.add(Color.magenta);
		agentColors.add(Color.orange);
		
		List <Color> goalColors = new ArrayList<Color>();
		goalColors.add(Color.gray);
		for(Color c : agentColors){
			goalColors.add(c.darker().darker());
		}
		
		v.addObjectClassPainter(GridGame.CLASSGOAL, new CellPainter(maxX, maxY, goalColors, 0));
		v.addObjectClassPainter(GridGame.CLASSAGENT, new CellPainter(maxX, maxY, agentColors, 1));
		v.addObjectClassPainter(GridGame.CLASSDIMVWALL, new WallPainter(maxX, maxY, true));
		v.addObjectClassPainter(GridGame.CLASSDIMHWALL, new WallPainter(maxX, maxY, false));
		
		return v;
	}
	
	
	
	/**
	 * A painter that can be used for either agent objects or goal objects.
	 * @author James MacGlashan
	 *
	 */
	static class CellPainter implements ObjectPainter{

		/**
		 * The width of the playing field
		 */
		int maxX;
		
		/**
		 * The height of the playing field
		 */
		int maxY;
		
		/**
		 * The color list that corresponds to each agent.
		 */
		List<Color> cols;
		
		/**
		 * The shape of the cell that should be drawn; 0 for a rectangle, 1 for a circle.
		 */
		int shape;
		
		
		/**
		 * Initializes the cell painter
		 * @param mx the width of the playing field
		 * @param my the height of the playing field
		 * @param cols the colors that correspond to each agent
		 * @param s the shape to paint; 0 for rectangles, 1 for circles.
		 */
		public CellPainter(int mx, int my, List <Color> cols, int s){
			this.maxX = mx;
			this.maxY = my;
			this.cols = cols;
			this.shape = s;
		}
		
		@Override
		public void paintObject(Graphics2D g2, State s, OldObjectInstance ob, float cWidth, float cHeight) {
			
			int colInd = 0;
			if(ob.getClassName().equals(GridGame.CLASSAGENT)){
				colInd = ob.getIntValForAttribute(GridGame.ATTPN);
			}
			else if(ob.getClassName().equals(GridGame.CLASSGOAL)){
				colInd = ob.getIntValForAttribute(GridGame.ATTGT);
			}
			
			g2.setColor(this.cols.get(colInd));
			
			float domainXScale = maxX;
			float domainYScale = maxY;
			
			//determine then normalized width
			float width = (1.0f / domainXScale) * cWidth;
			float height = (1.0f / domainYScale) * cHeight;
			
			float rx = ob.getIntValForAttribute(GridGame.ATTX)*width;
			float ry = cHeight - height - ob.getIntValForAttribute(GridGame.ATTY)*height;
			
			if(shape == 0){
				g2.fill(new Rectangle2D.Float(rx, ry, width, height));
			}
			else if(shape == 1){
				g2.fill(new Ellipse2D.Float(rx, ry, width, height));
			}
			
		}

		
		
		
	}
	
	
	
	/**
	 * Draws a wall object. Solid walls will be drawn with a solid line and semi-walls with a dashed line
	 * @author James MacGlashan
	 *
	 */
	static class WallPainter implements ObjectPainter{

		/**
		 * The width of the playing field
		 */
		int maxX;
		
		/**
		 * The height of the playing field
		 */
		int maxY;
		
		/**
		 * Whether this painter is for vertical walls (true) or horizontal walls (false).
		 */
		boolean vertical;
		
		
		/**
		 * Initializes the painter.
		 * @param mx the width of the playing field
		 * @param my the height of the playing field
		 * @param vert true if this painter is for vertical walls, false if for horizontal walls
		 */
		public WallPainter(int mx, int my, boolean vert){
			this.maxX = mx;
			this.maxY = my;
			this.vertical = vert;
		}
		
		@Override
		public void paintObject(Graphics2D g2, State s, OldObjectInstance ob, float cWidth, float cHeight) {
			
			int p0x, p0y, p1x, p1y;
			
			int wp = ob.getIntValForAttribute(GridGame.ATTP);
			int e1 = ob.getIntValForAttribute(GridGame.ATTE1);
			int e2 = ob.getIntValForAttribute(GridGame.ATTE2);
			
			if(vertical){
				p0x = p1x = wp;
				p0y = e1;
				p1y = e2+1;
			}
			else{
				p0y = p1y = wp;
				p0x = e1;
				p1x = e2+1;
			}
			
			float nx0 = (float)p0x / (float)maxX;
			float ny0 = 1.f - ((float)p0y / (float)maxY);
			
			float nx1 = (float)p1x / (float)maxX;
			float ny1 = 1.f - ((float)p1y / (float)maxY);
			
			
			g2.setColor(Color.black);
			
			int wt = ob.getIntValForAttribute(GridGame.ATTWT);
			if(wt == 0){
				g2.setStroke(new BasicStroke(10));
			}
			else if(wt == 1){
				g2.setStroke(new BasicStroke(10, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] {(cWidth/maxX)/5.f}, 0));
			}
			
			g2.drawLine((int)(nx0*cWidth), (int)(ny0*cHeight), (int)(nx1*cWidth), (int)(ny1*cHeight));
			
			
			
		}
		
		
		
		
	}

}
