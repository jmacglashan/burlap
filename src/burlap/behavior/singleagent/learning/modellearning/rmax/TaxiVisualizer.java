package burlap.behavior.singleagent.learning.modellearning.rmax;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import burlap.domain.stochasticgames.gridgame.GridGame;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.visualizer.ObjectPainter;
import burlap.oomdp.visualizer.StateRenderLayer;
import burlap.oomdp.visualizer.Visualizer;

public class TaxiVisualizer {

	
	public static Visualizer getVisualizer(int w, int h){
		Visualizer v = new Visualizer(getStateRenderLayer(w, h));
		return v;
		
	}
	
	
	public static StateRenderLayer getStateRenderLayer(int w, int h){
		
		StateRenderLayer rl = new StateRenderLayer();
		
		rl.addObjectClassPainter(TaxiDomain.LOCATIONCLASS, new LocationPainter(w, h, 0, true));
		rl.addObjectClassPainter(TaxiDomain.TAXICLASS, new CellPainter(1, Color.gray, w, h));
		rl.addObjectClassPainter(TaxiDomain.PASSENGERCLASS, new PassengerPainter(w, h, 1, false));
		rl.addObjectClassPainter(TaxiDomain.HWALLCLASS, new WallPainter(w, h, false));
		rl.addObjectClassPainter(TaxiDomain.VWALLCLASS, new WallPainter(w, h, true));
		
		return rl;
		
	}
	
	
	
	
	/**
	 * A painter for a grid world cell which will fill the cell with a given color and where the cell position
	 * is indicated by the x and y attribute for the mapped object instance
	 * @author James MacGlashan
	 *
	 */
	public static class CellPainter implements ObjectPainter{

		protected Color			col;
		protected int			dwidth;
		protected int			dheight;
		protected int			shape = 0; //0 for rectangle 1 for ellipse
		
		
		/**
		 * Initializes painter for a rectangle shape cell
		 * @param col the color to paint the cell
		 * @param map the wall map matrix where 1s indicate a wall in that cell and 0s indicate it is clear of walls
		 */
		public CellPainter(Color col, int w, int h) {
			this.col = col;
			this.dwidth = w;
			this.dheight = h;
		}
		
		/**
		 * Initializes painter with filling the cell with the given shape
		 * @param shape the shape with which to fill the cell. 0 for a rectangle, 1 for an ellipse.
		 * @param col the color to paint the cell
		 * @param map the wall map matrix where 1s indicate a wall in that cell and 0s indicate it is clear of walls
		 */
		public CellPainter(int shape, Color col, int w, int h) {
			this.col = col;
			this.dwidth = w;
			this.dheight = h;
			this.shape = shape;
		}

		@Override
		public void paintObject(Graphics2D g2, State s, ObjectInstance ob, float cWidth, float cHeight) {
			
			
			//set the color of the object
			g2.setColor(this.col);
			
			float domainXScale = this.dwidth;
			float domainYScale = this.dheight;
			
			//determine then normalized width
			float width = (1.0f / domainXScale) * cWidth;
			float height = (1.0f / domainYScale) * cHeight;
			
			float rx = ob.getDiscValForAttribute(TaxiDomain.XATT)*width;
			float ry = cHeight - height - ob.getDiscValForAttribute(TaxiDomain.YATT)*height;
			
			if(this.shape == 0){
				g2.fill(new Rectangle2D.Float(rx, ry, width, height));
			}
			else{
				g2.fill(new Ellipse2D.Float(rx, ry, width, height));
			}
			
		}
		
		
		
		
	}
	
	
	
	/**
	 * A painter for location objects which will fill the cell with a given color and where the cell position
	 * is indicated by the x and y attribute for the mapped object instance
	 * @author James MacGlashan
	 *
	 */
	public static class LocationPainter implements ObjectPainter{

		protected List<Color>	baseColors;
		protected int			dwidth;
		protected int			dheight;
		protected int			shape = 0; //0 for rectangle 1 for ellipse
		
		
		/**
		 * Initializes painter
		 * @param map the wall map matrix where 1s indicate a wall in that cell and 0s indicate it is clear of walls
		 */
		public LocationPainter(int w, int h, int shape, boolean darken) {
			this.dwidth = w;
			this.dheight = h;
			this.shape = shape;
			this.baseColors = new ArrayList<Color>(9);
			this.baseColors.add(Color.darkGray);
			this.baseColors.add(Color.red);
			this.baseColors.add(Color.green);
			this.baseColors.add(Color.blue);
			this.baseColors.add(Color.yellow);
			this.baseColors.add(Color.magenta);
			this.baseColors.add(Color.pink);
			this.baseColors.add(Color.orange);
			this.baseColors.add(Color.cyan);
			
			if(darken){
				List<Color> dcols = new ArrayList<Color>(9);
				for(Color c : this.baseColors){
					dcols.add(c.darker());
				}
				this.baseColors = dcols;
			}
			
		}

		@Override
		public void paintObject(Graphics2D g2, State s, ObjectInstance ob, float cWidth, float cHeight) {
			
			int type = ob.getDiscValForAttribute(TaxiDomain.LOCATIONATT);
			
			Color col = this.baseColors.get(type);
			
			
			//set the color of the object
			g2.setColor(col);
			
			float domainXScale = this.dwidth;
			float domainYScale = this.dheight;
			
			//determine then normalized width
			float width = (1.0f / domainXScale) * cWidth;
			float height = (1.0f / domainYScale) * cHeight;
			
			float rx = ob.getDiscValForAttribute(TaxiDomain.XATT)*width;
			float ry = cHeight - height - ob.getDiscValForAttribute(TaxiDomain.YATT)*height;
			
			if(this.shape == 0){
				g2.fill(new Rectangle2D.Float(rx, ry, width, height));
			}
			else{
				g2.fill(new Ellipse2D.Float(rx, ry, width, height));
			}
			
		}
		
		
	}
	
	
	
	
	public static class PassengerPainter implements ObjectPainter{

		protected List<Color>	baseColors;
		protected int			dwidth;
		protected int			dheight;
		protected int			shape = 0; //0 for rectangle 1 for ellipse
		
		
		/**
		 * Initializes painter
		 * @param map the wall map matrix where 1s indicate a wall in that cell and 0s indicate it is clear of walls
		 */
		public PassengerPainter(int w, int h, int shape, boolean darken) {
			this.dwidth = w;
			this.dheight = h;
			this.shape = shape;
			this.baseColors = new ArrayList<Color>(9);
			this.baseColors.add(Color.darkGray);
			this.baseColors.add(Color.red);
			this.baseColors.add(Color.green);
			this.baseColors.add(Color.blue);
			this.baseColors.add(Color.yellow);
			this.baseColors.add(Color.magenta);
			this.baseColors.add(Color.pink);
			this.baseColors.add(Color.orange);
			this.baseColors.add(Color.cyan);
			
			if(darken){
				List<Color> dcols = new ArrayList<Color>(9);
				for(Color c : this.baseColors){
					dcols.add(c.darker());
				}
				this.baseColors = dcols;
			}
			
		}

		@Override
		public void paintObject(Graphics2D g2, State s, ObjectInstance ob, float cWidth, float cHeight) {
			
			int type = ob.getDiscValForAttribute(TaxiDomain.LOCATIONATT);
			
			Color col = this.baseColors.get(type);
			
			
			//set the color of the object
			g2.setColor(col);
			
			float domainXScale = this.dwidth;
			float domainYScale = this.dheight;
			
			//determine then normalized width
			float width = (1.0f / domainXScale) * cWidth;
			float height = (1.0f / domainYScale) * cHeight;
			
			float rx = ob.getDiscValForAttribute(TaxiDomain.XATT)*width;
			float ry = cHeight - height - ob.getDiscValForAttribute(TaxiDomain.YATT)*height;
			
			float rcx = rx + width/2f;
			float rcy = ry + height/2f;
			
			//now scale
			float sfactor = 0.8f;
			
			int inTaxi = ob.getDiscValForAttribute(TaxiDomain.INTAXIATT);
			if(inTaxi == 1){
				sfactor = 0.5f;
			}
			
			float swidth = width*sfactor;
			float sheight = height*sfactor;
			
			float srx = rcx-(swidth/2f);
			float sry = rcy-(sheight/2f);
			
			if(this.shape == 0){
				g2.fill(new Rectangle2D.Float(srx, sry, swidth, sheight));
			}
			else{
				g2.fill(new Ellipse2D.Float(srx, sry, swidth, sheight));
			}
			
		}
		
		
	}
	
	
	
	public static class WallPainter implements ObjectPainter{

		int maxX;
		int maxY;
		boolean vertical;
		
		
		public WallPainter(int w, int h, boolean vertical){
			this.maxX = w;
			this.maxY = h;
			this.vertical = vertical;
		}
		
		@Override
		public void paintObject(Graphics2D g2, State s, ObjectInstance ob,
				float cWidth, float cHeight) {
			
			int p0x, p0y, p1x, p1y;
			
			int wp = ob.getDiscValForAttribute(TaxiDomain.WALLOFFSETATT);
			int e1 = ob.getDiscValForAttribute(TaxiDomain.WALLMINATT);
			int e2 = ob.getDiscValForAttribute(TaxiDomain.WALLMAXATT);
			
			if(vertical){
				p0x = p1x = wp;
				p0y = e1;
				p1y = e2;
			}
			else{
				p0y = p1y = wp;
				p0x = e1;
				p1x = e2;
			}
			
			float nx0 = (float)p0x / (float)maxX;
			float ny0 = 1.f - ((float)p0y / (float)maxY);
			
			float nx1 = (float)p1x / (float)maxX;
			float ny1 = 1.f - ((float)p1y / (float)maxY);
			
			
			g2.setColor(Color.black);
			
			
			g2.setStroke(new BasicStroke(10));
			
			
			g2.drawLine((int)(nx0*cWidth), (int)(ny0*cHeight), (int)(nx1*cWidth), (int)(ny1*cHeight));
			
			
		}
		
		
		
	}
	
}
