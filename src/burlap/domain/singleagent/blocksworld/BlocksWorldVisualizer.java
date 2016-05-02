package burlap.domain.singleagent.blocksworld;

import burlap.oomdp.core.oo.state.OOState;
import burlap.oomdp.core.oo.state.ObjectInstance;
import burlap.oomdp.visualizer.OOStatePainter;
import burlap.oomdp.visualizer.ObjectPainter;
import burlap.oomdp.visualizer.StateRenderLayer;
import burlap.oomdp.visualizer.Visualizer;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static burlap.domain.singleagent.blocksworld.BlocksWorld.CLASS_BLOCK;

public class BlocksWorldVisualizer {

    private BlocksWorldVisualizer() {
        // do nothing
    }
    
	
	/**
	 * Returns a 2D Visualizer canvas object to visualize {@link BlocksWorld} states.
	 * @return the visualizer.
	 */
	public static Visualizer getVisualizer(){
		
		return getVisualizer(12);
	}
	
	
	/**
	 * Returns a 2D Visualizer canvas object to visualize {@link BlocksWorld} states where the name of the block is rendered at the provided font
	 * point size.
	 * @param fontSize the size of the font to use when rendering the name of a block object.
	 * @return the visualizer.
	 */
	public static Visualizer getVisualizer(int fontSize){
		
		Visualizer v = new Visualizer(getStateRenderLayer(fontSize));
		return v;
	}

	public static StateRenderLayer getStateRenderLayer(int fontSize){
		StateRenderLayer srl = new StateRenderLayer();
		OOStatePainter ooStatePainter = new OOStatePainter();
		srl.addStatePainter(ooStatePainter);
		ooStatePainter.addObjectClassPainter(CLASS_BLOCK, new BlockPainter(fontSize));
		return srl;
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
		public void paintObject(Graphics2D g2, OOState s, ObjectInstance ob, float cWidth, float cHeight) {

			List <ObjectInstance> objects = s.objects();
			List <String> obNames = new ArrayList<String>(objects.size());
			for(ObjectInstance o : objects){
				obNames.add(o.name());
			}
			Collections.sort(obNames);

			String indName = this.getStackBottom((OOState)s, (BlocksWorldBlock)ob);

			int ind = obNames.indexOf(indName);
			int maxSize = obNames.size();

			float blockWidth = cWidth / maxSize;
			float blockHeight = cHeight / maxSize;

			float hGap = 10;

			g2.setColor(((BlocksWorldBlock)ob).color);

			float rx = ind*blockWidth;
			float ry = cHeight - blockHeight - this.getHeight(s, (BlocksWorldBlock)ob)*blockHeight;

			g2.fill(new Rectangle2D.Float(rx + (hGap), ry, blockWidth - 2*hGap, blockHeight));


			g2.setColor(Color.black);
			g2.setFont(new Font("Helvetica", Font.PLAIN, fontSize));

			String valueString = ob.name();
			int stringLen = (int)g2.getFontMetrics().getStringBounds(valueString, g2).getWidth();
			int stringHeight = (int)g2.getFontMetrics().getStringBounds(valueString, g2).getHeight();
			int stringX = (int)((rx + (blockWidth/2)) - (stringLen/2));
			int stringY = (int)((ry + (blockHeight/2)) + (stringHeight/2));

			g2.drawString(valueString, stringX, stringY);

		}

		
		protected String getStackBottom(OOState s, BlocksWorldBlock ob){
			if (ob.onTable()){
				return ob.name();
			}
			return getStackBottom(s, (BlocksWorldBlock)s.object(ob.on));
		}
		
		protected int getHeight(OOState s, BlocksWorldBlock ob){
			if (ob.onTable()){
				return 0;
			}
			return 1 + this.getHeight(s, (BlocksWorldBlock)s.object(ob.on));
		}

		
		
	}
	
}
