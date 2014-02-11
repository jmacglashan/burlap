package burlap.domain.singleagent.minecraft;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.visualizer.ObjectPainter;
import burlap.oomdp.visualizer.StaticPainter;
import burlap.oomdp.visualizer.Visualizer;


public class MinecraftVisualizer {

	public static Visualizer getVisualizer(Domain d, int [][] map){
		
		Visualizer v = new Visualizer();
		
		v.addStaticPainter(new MapPainter(d, map));
		v.addObjectClassPainter(MinecraftDomain.CLASSGOAL, new CellPainter(Color.blue, map));
		v.addObjectClassPainter(MinecraftDomain.CLASSAGENT, new CellPainter(Color.red, map));
		
		return v;
	}
	
	
	
	public static class MapPainter implements StaticPainter{

		protected int 				dwidth;
		protected int 				dheight;
		protected int [][] 			map; // Can only visualize in 2d now.
		
		public MapPainter(Domain domain, int [][] map) {
			this.dwidth = map.length;
			this.dheight = map[0].length;
			this.map = map;
		}

		@Override
		public void paint(Graphics2D g2, State s, float cWidth, float cHeight) {
			
			//draw the walls; make them black
			g2.setColor(Color.black);
			
			float domainXScale = this.dwidth;
			float domainYScale = this.dheight;
			
			//determine then normalized width
			float width = (1.0f / domainXScale) * cWidth;
			float height = (1.0f / domainYScale) * cHeight;
			
			//pass through each cell of the map and if it is a wall, draw it
			for(int i = 0; i < this.dwidth; i++){
				for(int j = 0; j < this.dheight; j++){
					
					if(this.map[i][j] == 1){
					
						float rx = i*width;
						float ry = cHeight - height - j*height;
					
						g2.fill(new Rectangle2D.Float(rx, ry, width, height));
						
					}
					
				}
			}
			
		}
		
		
	}
	
	
	
	public static class CellPainter implements ObjectPainter{

		protected Color			col;
		protected int			dwidth;
		protected int			dheight;
		protected int [][]		map;
		
		public CellPainter(Color col, int [][] map) {
			this.col = col;
			this.dwidth = map.length;
			this.dheight = map[0].length;
			this.map = map;
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
			
			float rx = ob.getDiscValForAttribute(MinecraftDomain.ATTX)*width;
			float ry = cHeight - height - ob.getDiscValForAttribute(MinecraftDomain.ATTY)*height;
			
			g2.fill(new Rectangle2D.Float(rx, ry, width, height));
			
		}
		
		
		
		
	}
	
	
}
