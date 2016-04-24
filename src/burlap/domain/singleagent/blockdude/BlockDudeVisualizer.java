package burlap.domain.singleagent.blockdude;

import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.visualizer.ObjectPainter;
import burlap.oomdp.visualizer.StateRenderLayer;
import burlap.oomdp.visualizer.Visualizer;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * A state visualizer for {@link burlap.domain.singleagent.blockdude.BlockDude}. The agent is a blue square with a gold eye
 * indicating the direction it's facing; bricks are rendered in green; the exit in black, and movable blocks in grey.
 * @author James MacGlashan.
 */
public class BlockDudeVisualizer {

    private BlockDudeVisualizer() {
        // do nothing
    }

	/**
	 * Returns a {@link burlap.oomdp.visualizer.Visualizer} for {@link burlap.domain.singleagent.blockdude.BlockDude}.
	 * @param maxx the max x dimensionality of the world
	 * @param maxy the max y dimensionality of the world
	 * @return a {@link burlap.oomdp.visualizer.Visualizer} for {@link burlap.domain.singleagent.blockdude.BlockDude}
	 */
	public static Visualizer getVisualizer(int maxx, int maxy){
		return new Visualizer(getStateRenderLayer(maxx, maxy));
	}

	/**
	 * Returns a {@link burlap.oomdp.visualizer.StateRenderLayer} for {@link burlap.domain.singleagent.blockdude.BlockDudeVisualizer}.
	 * @param maxx the max x dimensionality of the world
	 * @param maxy the max y dimensionality of the world
	 * @return a {@link burlap.oomdp.visualizer.StateRenderLayer} for {@link burlap.domain.singleagent.blockdude.BlockDudeVisualizer}.
	 */
	public static StateRenderLayer getStateRenderLayer(int maxx, int maxy){

		StateRenderLayer srl = new StateRenderLayer();
		srl.addObjectClassPainter(BlockDude.CLASSBRICKS, new BricksPainter(maxx, maxy));
		srl.addObjectClassPainter(BlockDude.CLASSAGENT, new AgentPainter(maxx, maxy));
		srl.addObjectClassPainter(BlockDude.CLASSEXIT, new ExitPainter(maxx, maxy));
		srl.addObjectClassPainter(BlockDude.CLASSBLOCK, new BlockPainter(maxx, maxy));

		return srl;
	}


	/**
	 * A class for rendering the agent as a blue square with a gold eye indicating the direction its facing.
	 */
	public static class AgentPainter implements ObjectPainter {

		public int minx = 0;
		public int miny = 0;

		public int maxx;
		public int maxy;

		/**
		 * Initializes.
		 * @param maxx the max x dimensionality of the world
		 * @param maxy the max y dimensionality of the world
		 */
		public AgentPainter(int maxx, int maxy){
			this.maxx = maxx;
			this.maxy = maxy;
		}

		@Override
		public void paintObject(Graphics2D g2, State s, ObjectInstance ob,
								float cWidth, float cHeight) {

			g2.setColor(Color.blue);

			float domainXScale = (maxx) - minx;
			float domainYScale = (maxy) - miny;

			//determine then normalized width
			float width = (1.0f / domainXScale) * cWidth;
			float height = (1.0f / domainYScale) * cHeight;

			float rx = ob.getIntValForAttribute(BlockDude.ATTX)*width;
			float ry = cHeight - height - ob.getIntValForAttribute(BlockDude.ATTY)*height;

			g2.fill(new Rectangle2D.Float(rx, ry, width, height));


			//draw eye for showing the direction of the agent
			g2.setColor(Color.orange);
			float eyeWidth = width*0.25f;
			float eyeHeight = height*0.25f;

			float ex = rx;
			if(ob.getIntValForAttribute(BlockDude.ATTDIR) == 1){
				ex = (rx+width) - eyeWidth;
			}

			float ey = ry + 0.2f*height;

			g2.fill(new Rectangle2D.Float(ex, ey, eyeWidth, eyeHeight));

		}


	}


	/**
	 * A class for rendering a block as a grey square
	 */
	public static class BlockPainter implements ObjectPainter{

		public int minx = 0;
		public int miny = 0;

		public int maxx;
		public int maxy;

		/**
		 * Initializes.
		 * @param maxx the max x dimensionality of the world
		 * @param maxy the max y dimensionality of the world
		 */
		public BlockPainter(int maxx, int maxy){
			this.maxx = maxx;
			this.maxy = maxy;
		}


		@Override
		public void paintObject(Graphics2D g2, State s, ObjectInstance ob,
								float cWidth, float cHeight) {

			g2.setColor(Color.gray);

			float domainXScale = (maxx) - minx;
			float domainYScale = (maxy) - miny;

			//determine then normalized width
			float width = (1.0f / domainXScale) * cWidth;
			float height = (1.0f / domainYScale) * cHeight;

			float rx = ob.getIntValForAttribute(BlockDude.ATTX)*width;
			float ry = cHeight - height - ob.getIntValForAttribute(BlockDude.ATTY)*height;

			g2.fill(new Rectangle2D.Float(rx, ry, width, height));

		}


	}


	/**
	 * A class for rendering an exit with a black square
	 */
	public static class ExitPainter implements ObjectPainter{


		public int minx = 0;
		public int miny = 0;

		public int maxx;
		public int maxy;

		/**
		 * Initializes.
		 * @param maxx the max x dimensionality of the world
		 * @param maxy the max y dimensionality of the world
		 */
		public ExitPainter(int maxx, int maxy){

			this.maxx = maxx;
			this.maxy = maxy;
		}

		@Override
		public void paintObject(Graphics2D g2, State s, ObjectInstance ob,
								float cWidth, float cHeight) {

			g2.setColor(Color.black);

			float domainXScale = (maxx) - minx;
			float domainYScale = (maxy) - miny;

			//determine then normalized width
			float width = (1.0f / domainXScale) * cWidth;
			float height = (1.0f / domainYScale) * cHeight;

			float rx = ob.getIntValForAttribute(BlockDude.ATTX)*width;
			float ry = cHeight - height - ob.getIntValForAttribute(BlockDude.ATTY)*height;

			g2.fill(new Rectangle2D.Float(rx, ry, width, height));

		}



	}


	/**
	 * A class for rendering bricks as green squares. Since all bricks are specified in a single 1D array, rather than
	 * a separate object for each, this class will iterate through the array and paint each brick.
	 */
	public static class BricksPainter implements ObjectPainter{

		public int minx = 0;
		public int miny = 0;

		public int maxx;
		public int maxy;

		/**
		 * Initializes.
		 * @param maxx the max x dimensionality of the world
		 * @param maxy the max y dimensionality of the world
		 */
		public BricksPainter(int maxx, int maxy){
			this.maxx = maxx;
			this.maxy = maxy;
		}


		@Override
		public void paintObject(Graphics2D g2, State s, ObjectInstance ob, float cWidth, float cHeight) {

			g2.setColor(Color.green);

			float domainXScale = (maxx) - minx;
			float domainYScale = (maxy) - miny;

			//determine then normalized width
			float width = (1.0f / domainXScale) * cWidth;
			float height = (1.0f / domainYScale) * cHeight;

			int [] map = ob.getIntArrayValForAttribute(BlockDude.ATTMAP);

			for(int i = 0; i < map.length; i++){

				if(map[i] == 1) {

					int x = i % this.maxx;
					int y = i / this.maxx;

					float rx = x * width;
					float ry = cHeight - height - y * height;

					g2.fill(new Rectangle2D.Float(rx, ry, width, height));

				}

			}

		}
	}

}
