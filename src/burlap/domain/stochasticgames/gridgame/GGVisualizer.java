package burlap.domain.stochasticgames.gridgame;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.visualizer.ObjectPainter;
import burlap.oomdp.visualizer.Visualizer;



public class GGVisualizer {

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
	
	
	static class CellPainter implements ObjectPainter{

		int maxX;
		int maxY;
		List<Color> cols;
		int shape;
		
		public CellPainter(int mx, int my, List <Color> cols, int s){
			this.maxX = mx;
			this.maxY = my;
			this.cols = cols;
			this.shape = s;
		}
		
		@Override
		public void paintObject(Graphics2D g2, State s, ObjectInstance ob, float cWidth, float cHeight) {
			
			int colInd = 0;
			if(ob.getTrueClassName().equals(GridGame.CLASSAGENT)){
				colInd = ob.getDiscValForAttribute(GridGame.ATTPN);
			}
			else if(ob.getTrueClassName().equals(GridGame.CLASSGOAL)){
				colInd = ob.getDiscValForAttribute(GridGame.ATTGT);
			}
			
			g2.setColor(this.cols.get(colInd));
			
			float domainXScale = maxX;
			float domainYScale = maxY;
			
			//determine then normalized width
			float width = (1.0f / domainXScale) * cWidth;
			float height = (1.0f / domainYScale) * cHeight;
			
			float rx = ob.getDiscValForAttribute(GridGame.ATTX)*width;
			float ry = cHeight - height - ob.getDiscValForAttribute(GridGame.ATTY)*height;
			
			if(shape == 0){
				g2.fill(new Rectangle2D.Float(rx, ry, width, height));
			}
			else if(shape == 1){
				g2.fill(new Ellipse2D.Float(rx, ry, width, height));
			}
			
		}

		
		
		
	}
	
	
	
	static class WallPainter implements ObjectPainter{

		int maxX;
		int maxY;
		boolean vertical;
		
		public WallPainter(int mx, int my, boolean vert){
			this.maxX = mx;
			this.maxY = my;
			this.vertical = vert;
		}
		
		@Override
		public void paintObject(Graphics2D g2, State s, ObjectInstance ob, float cWidth, float cHeight) {
			
			int p0x, p0y, p1x, p1y;
			
			int wp = ob.getDiscValForAttribute(GridGame.ATTP);
			int e1 = ob.getDiscValForAttribute(GridGame.ATTE1);
			int e2 = ob.getDiscValForAttribute(GridGame.ATTE2);
			
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
			
			int wt = ob.getDiscValForAttribute(GridGame.ATTWT);
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
