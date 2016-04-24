package burlap.domain.singleagent.gridworld.macro;

import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.domain.singleagent.gridworld.GridWorldVisualizer;
import burlap.oomdp.core.states.State;
import burlap.oomdp.visualizer.StateRenderLayer;
import burlap.oomdp.visualizer.StaticPainter;
import burlap.oomdp.visualizer.Visualizer;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Map;

/**
 * A class for visualizing the reward weights assigned to a Macro-cell in a Macro-cell grid world. Reward weights provided are first normalized to lie between 0 and 1.
 * Macro cells are filled in with color that ranges from white if the reward weight is 0, to blue if the reward weight 1. The agent is rendered as a grey circle.
 * @author Stephen Brawner and Mark Ho; modified by James MacGlashan
 *
 */
public class MacroCellVisualizer {

	

	/**
	 * Returns a {@link Visualizer} for the Macro-Cell GridWorld Visualizer and reward weights associated with a set of MacroCell Propositional functions.
	 * @param map a map of the grid world
	 * @param propFunctions the macro cell propositional functions
	 * @param rewardMap the reward weights associated with the propositional function names
	 * @return a {@link Visualizer}
	 */
	public static Visualizer getVisualizer(int [][] map, MacroCellGridWorld.InMacroCellPF[] propFunctions, Map<String, Double> rewardMap) {
		
		StateRenderLayer r = getStateRenderLayer(map, propFunctions, rewardMap);
		return new Visualizer(r);
	}
	
	
	/**
	 * Returns a {@link StateRenderLayer} for the Macro-Cell GridWorld Visualizer and reward weights associated with a set of MacroCell Propositional functions.
	 * @param map a map of the grid world
	 * @param propFunctions the macro cell propositional functions
	 * @param rewardMap the reward weights associated with the propositional function names
	 * @return a {@link StateRenderLayer} 
	 */
	public static StateRenderLayer getStateRenderLayer(int [][] map, MacroCellGridWorld.InMacroCellPF[] propFunctions, Map<String, Double> rewardMap){
		StateRenderLayer r = new StateRenderLayer();
		
		r.addStaticPainter(new GridWorldVisualizer.MapPainter(map));
		r.addStaticPainter(new MacroCellRewardWeightPainter(map, propFunctions, rewardMap));
		r.addObjectClassPainter(GridWorldDomain.CLASSAGENT, new GridWorldVisualizer.CellPainter(1, Color.gray, map));
		
		return r;
	}

	
	/**
	 * Class for painting the macro cells a color between white and blue, where strong blue indicates strong reward weights.
	 * @author Stephen Brawner and Mark Ho; modified by James MacGlashan
	 *
	 */
	public static class MacroCellRewardWeightPainter implements StaticPainter{

		protected int 				dwidth;
		protected int 				dheight;
		protected double [][]		rewardMap;

		public MacroCellRewardWeightPainter(int [][] map, MacroCellGridWorld.InMacroCellPF[] propFunctions, Map<String, Double> rewardMap) {
			this.dwidth = map.length;
			this.dheight = map[0].length;
			this.rewardMap = new double[map.length][map[0].length];
			for (int i = 0; i < this.rewardMap.length; i++) {
				for (int j = 0; j < this.rewardMap[0].length; j++) {
					for (int k = 0; k < propFunctions.length; k++) {
						if (propFunctions[k].isTrue(i, j)) {
							this.rewardMap[i][j] += rewardMap.get(propFunctions[k].getName());
						}
					}
				}
			}
			
			
			//force normalization of reward weights
			double sumRW = 0.;
			for(int i = 0; i < this.rewardMap.length; i++){
				for(int j = 0; j < this.rewardMap[0].length; j++){
					sumRW += this.rewardMap[i][j]*this.rewardMap[i][j];
				}
			}
			
			sumRW = Math.sqrt(sumRW);
			
			for(int i = 0; i < this.rewardMap.length; i++){
				for(int j = 0; j < this.rewardMap[0].length; j++){
					this.rewardMap[i][j] = this.rewardMap[i][j] / sumRW;
				}
			}
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

					double invRange = 1. - this.rewardMap[i][j];
					int colRG = (int)(255*invRange);
					g2.setColor(new Color(colRG, colRG, 255));
					float rx = i*width;
					float ry = cHeight - height - j*height;
					g2.fill(new Rectangle2D.Float(rx, ry, width, height));						
				}
			}	
		}	
	}	

}
