package burlap.domain.singleagent.blockdude;

import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.visualizer.OOStatePainter;
import burlap.mdp.visualizer.ObjectPainter;
import burlap.mdp.visualizer.StateRenderLayer;
import burlap.mdp.visualizer.Visualizer;

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
	 * Returns a {@link burlap.mdp.visualizer.Visualizer} for {@link burlap.domain.singleagent.blockdude.BlockDude}.
	 * @param maxx the max x dimensionality of the world
	 * @param maxy the max y dimensionality of the world
	 * @return a {@link burlap.mdp.visualizer.Visualizer} for {@link burlap.domain.singleagent.blockdude.BlockDude}
	 */
	public static Visualizer getVisualizer(int maxx, int maxy){
		Visualizer v = new Visualizer(getStateRenderLayer(maxx, maxy));
		return v;
	}

	/**
	 * Returns a {@link burlap.mdp.visualizer.StateRenderLayer} for {@link burlap.domain.singleagent.blockdude.BlockDudeVisualizer}.
	 * @param maxx the max x dimensionality of the world
	 * @param maxy the max y dimensionality of the world
	 * @return a {@link burlap.mdp.visualizer.StateRenderLayer} for {@link burlap.domain.singleagent.blockdude.BlockDudeVisualizer}.
	 */
	public static StateRenderLayer getStateRenderLayer(int maxx, int maxy){

		StateRenderLayer srl = new StateRenderLayer();

		OOStatePainter oopainter = new OOStatePainter();
		srl.addStatePainter(oopainter);

		oopainter.addObjectClassPainter(BlockDude.CLASS_MAP, new BricksPainter(maxx, maxy));
		oopainter.addObjectClassPainter(BlockDude.CLASS_AGENT, new AgentPainter(maxx, maxy));
		oopainter.addObjectClassPainter(BlockDude.CLASS_EXIT, new ExitPainter(maxx, maxy));
		oopainter.addObjectClassPainter(BlockDude.CLASS_BLOCK, new BlockPainter(maxx, maxy));

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
		public void paintObject(Graphics2D g2, OOState s, ObjectInstance ob,
								float cWidth, float cHeight) {

			g2.setColor(Color.blue);

			float domainXScale = (maxx) - minx;
			float domainYScale = (maxy) - miny;

			//determine then normalized width
			float width = (1.0f / domainXScale) * cWidth;
			float height = (1.0f / domainYScale) * cHeight;

			float rx = (Integer)ob.get(BlockDude.VAR_X)*width;
			float ry = cHeight - height - (Integer)ob.get(BlockDude.VAR_Y)*height;

			g2.fill(new Rectangle2D.Float(rx, ry, width, height));


			//draw eye for showing the direction of the agent
			g2.setColor(Color.orange);
			float eyeWidth = width*0.25f;
			float eyeHeight = height*0.25f;

			float ex = rx;
			if((Integer)ob.get(BlockDude.VAR_DIR) == 1){
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
		public void paintObject(Graphics2D g2, OOState s, ObjectInstance ob,
								float cWidth, float cHeight) {

			g2.setColor(Color.gray);

			float domainXScale = (maxx) - minx;
			float domainYScale = (maxy) - miny;

			//determine then normalized width
			float width = (1.0f / domainXScale) * cWidth;
			float height = (1.0f / domainYScale) * cHeight;

			float rx = (Integer)ob.get(BlockDude.VAR_X)*width;
			float ry = cHeight - height - (Integer)ob.get(BlockDude.VAR_Y)*height;

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
		public void paintObject(Graphics2D g2, OOState s, ObjectInstance ob,
								float cWidth, float cHeight) {

			g2.setColor(Color.black);

			float domainXScale = (maxx) - minx;
			float domainYScale = (maxy) - miny;

			//determine then normalized width
			float width = (1.0f / domainXScale) * cWidth;
			float height = (1.0f / domainYScale) * cHeight;

			float rx = (Integer)ob.get(BlockDude.VAR_X)*width;
			float ry = cHeight - height - (Integer)ob.get(BlockDude.VAR_Y)*height;

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
		public void paintObject(Graphics2D g2, OOState s, ObjectInstance ob, float cWidth, float cHeight) {

			g2.setColor(Color.green);

			float domainXScale = (maxx) - minx;
			float domainYScale = (maxy) - miny;

			//determine then normalized width
			float width = (1.0f / domainXScale) * cWidth;
			float height = (1.0f / domainYScale) * cHeight;


			int [][] map = (int[][])ob.get(BlockDude.VAR_MAP);

			for(int x = 0; x < map.length; x++){
				for(int y = 0; y < map[0].length; y++){
					float rx = x * width;
					float ry = cHeight - height - y * height;

					if(map[x][y] == 1) {
						g2.fill(new Rectangle2D.Float(rx, ry, width, height));
					}
				}
			}

		}
	}

}
