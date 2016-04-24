package burlap.domain.singleagent.blocksworld;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.visualizer.ObjectPainter;
import burlap.oomdp.visualizer.Visualizer;

public class BlocksWorldVisualizer {

    private BlocksWorldVisualizer() {
        // do nothing
    }
    
	
	/**
	 * Returns a 2D Visualizer canvas object to visualize {@link BlocksWorld} states.
	 * @return the visualizer.
	 */
	public static Visualizer getVisualizer(){
		
		Visualizer v = new Visualizer();
		v.addObjectClassPainter(BlocksWorld.CLASSBLOCK, new BlockPainter());
		return v;
	}
	
	
	/**
	 * Returns a 2D Visualizer canvas object to visualize {@link BlocksWorld} states where the name of the block is rendered at the provided font
	 * point size.
	 * @param fontSize the size of the font to use when rendering the name of a block object.
	 * @return the visualizer.
	 */
	public static Visualizer getVisualizer(int fontSize){
		
		Visualizer v = new Visualizer();
		v.addObjectClassPainter(BlocksWorld.CLASSBLOCK, new BlockPainter(fontSize));
		return v;
	}
	
	
	
	/**
	 * Paints blocks as a rectangle scaled to a size necessary to be able to show all blocks on the table within the canvas width or all blocks
	 * stacked on each other within the canvas height. The name of the block object identifier will be painted on the block and the color of the
	 * rectangle corresponds to the color attribtue of the block.
	 * @author James MacGlashan
	 *
	 */
	public static class BlockPainter implements ObjectPainter{

		int fontSize = 12;
		
		public BlockPainter(){
			
		}
		
		/**
		 * Initializes with a font size for rendering the name of the block object.
		 * @param fontSize a font size for rendering the name of the block object.
		 */
		public BlockPainter(int fontSize){
			this.fontSize = fontSize;
		}
		
		
		@Override
		public void paintObject(Graphics2D g2, State s, ObjectInstance ob,
				float cWidth, float cHeight) {
			
			List <ObjectInstance> objects = s.getAllObjects();
			List <String> obNames = new ArrayList<String>(objects.size());
			for(ObjectInstance o : objects){
				obNames.add(o.getName());
			}
			Collections.sort(obNames);
			
			String indName = this.getStackBottom(s, ob);
			
			int ind = obNames.indexOf(indName);
			int maxSize = obNames.size();
			
			float blockWidth = cWidth / maxSize;
			float blockHeight = cHeight / maxSize;
			
			float hGap = 10;
			
			g2.setColor(this.getColorForString(ob.getStringValForAttribute(BlocksWorld.ATTCOLOR)));
			
			float rx = ind*blockWidth;
			float ry = cHeight - blockHeight - this.getHeight(s, ob)*blockHeight;
			
			g2.fill(new Rectangle2D.Float(rx + (hGap), ry, blockWidth - 2*hGap, blockHeight));
			
			
			g2.setColor(Color.black);
			g2.setFont(new Font("Helvetica", Font.PLAIN, fontSize));
			
			String valueString = ob.getName();
			int stringLen = (int)g2.getFontMetrics().getStringBounds(valueString, g2).getWidth();
			int stringHeight = (int)g2.getFontMetrics().getStringBounds(valueString, g2).getHeight();
			int stringX = (int)((rx + (blockWidth/2)) - (stringLen/2));
			int stringY = (int)((ry + (blockHeight/2)) + (stringHeight/2));
			
			g2.drawString(valueString, stringX, stringY);
			
		}
		
		protected String getStackBottom(State s, ObjectInstance ob){
			if(ob.getIntValForAttribute(BlocksWorld.ATTONTABLE) == 1){
				return ob.getName();
			}
			return this.getStackBottom(s, s.getObject(ob.getStringValForAttribute(BlocksWorld.ATTONBLOCK)));
		}
		
		protected int getHeight(State s, ObjectInstance ob){
			if(ob.getIntValForAttribute(BlocksWorld.ATTONTABLE) == 1){
				return 0;
			}
			return 1 + this.getHeight(s, s.getObject(ob.getStringValForAttribute(BlocksWorld.ATTONBLOCK)));
		}
		
		protected Color getColorForString(String colName){
			if(colName.equals(BlocksWorld.COLORRED)){
				return Color.RED;
			}
			if(colName.equals(BlocksWorld.COLORGREEN)){
				return Color.GREEN;
			}
			if(colName.equals(BlocksWorld.COLORBLUE)){
				return Color.BLUE;
			}
			return null;
			
		}
		
		
		
	}
	
}
