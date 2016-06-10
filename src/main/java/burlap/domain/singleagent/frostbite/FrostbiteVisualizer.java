package burlap.domain.singleagent.frostbite;

import burlap.mdp.core.oo.state.OOState;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.State;
import burlap.visualizer.OOStatePainter;
import burlap.visualizer.ObjectPainter;
import burlap.visualizer.StatePainter;
import burlap.visualizer.Visualizer;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * @author Phillipe Morere
 */
public class FrostbiteVisualizer {

	private final static Color activatedPlatformColor = new Color(0.73333335f, 0.84313726f, 0.9411765f);
	private final static Color iglooColor = new Color(0.7294118f, 0.827451f, 0.84313726f);
	private final static Color waterColor = new Color(0.34509805f, 0.43529412f, 0.60784316f);

	private FrostbiteVisualizer() {
	    // do nothing
	}


	public static Visualizer getVisualizer() {
		return getVisualizer(5);
	}
	/**
	 * Returns a visualizer for a lunar lander domain.
	 *
	 * @param scale the scale of the domain
	 * @return a visualizer for a lunar lander domain.
	 */
	public static Visualizer getVisualizer(int scale) {

		final int gameHeight = 130 * scale;
		final int gameWidth = 160 * scale;
		final int iceHeight = gameHeight / 4;
		int agentSize = 8 * scale;

		Visualizer v = new Visualizer();


		v.addStatePainter(new StatePainter() {
			@Override
			public void paint(Graphics2D g2, State s, float cWidth, float cHeight) {
				g2.setColor(waterColor);
				g2.fill(new Rectangle2D.Double(0, 0, gameWidth, gameHeight));
				g2.setColor(Color.white);
				g2.fill(new Rectangle2D.Double(0, 0, gameWidth, iceHeight));
			}
		});

		OOStatePainter ooStatePainter = new OOStatePainter();
		v.addStatePainter(ooStatePainter);

		ooStatePainter.addObjectClassPainter(FrostbiteDomain.CLASS_PLATFORM, new PlatformPainter(gameWidth));
		ooStatePainter.addObjectClassPainter(FrostbiteDomain.CLASS_IGLOO, new IglooPainter(gameWidth, gameHeight));
		ooStatePainter.addObjectClassPainter(FrostbiteDomain.CLASS_AGENT, new AgentPainter(agentSize));

		return v;
	}

	public static class PlatformPainter implements ObjectPainter {

		int gameWidth;

		public PlatformPainter(int gameWidth) {
			this.gameWidth = gameWidth;
		}

		@Override
		public void paintObject(Graphics2D g2, OOState s, ObjectInstance ob,
								float cWidth, float cHeight) {



			int x = (Integer)ob.get(FrostbiteDomain.VAR_X);
			int y = (Integer)ob.get(FrostbiteDomain.VAR_Y);
			int size = (Integer)ob.get(FrostbiteDomain.VAR_SIZE);
			boolean activated = (Boolean)ob.get(FrostbiteDomain.VAR_ACTIVATED);
			if (activated)
				g2.setColor(activatedPlatformColor);
			else
				g2.setColor(Color.white);

			g2.fill(new Rectangle2D.Double(x, y, size, size));

			if (x + size > gameWidth)
				g2.fill(new Rectangle2D.Double(x - gameWidth, y, size, size));
			else if (x < 0)
				g2.fill(new Rectangle2D.Double(x + gameWidth, y, size, size));
		}
	}

	public static class AgentPainter implements ObjectPainter {

		protected int agentSize;

		public AgentPainter(int agentSize) {
			this.agentSize = agentSize;
		}

		@Override
		public void paintObject(Graphics2D g2, OOState s, ObjectInstance ob,
								float cWidth, float cHeight) {

			g2.setColor(Color.black);

			int x = (Integer)ob.get(FrostbiteDomain.VAR_X);
			int y = (Integer)ob.get(FrostbiteDomain.VAR_Y);
			int size = agentSize;

			g2.fill(new Rectangle2D.Double(x, y, size, size));
		}
	}

	public static class IglooPainter implements ObjectPainter {

		protected int gameWidth;
		protected int gameHeight;

		public IglooPainter(int gameWidth, int gameHeight) {
			this.gameWidth = gameWidth;
			this.gameHeight = gameHeight;
		}

		@Override
		public void paintObject(Graphics2D g2, OOState s, ObjectInstance ob,
								float cWidth, float cHeight) {

			g2.setColor(iglooColor);

			int building = (Integer)ob.get(FrostbiteDomain.VAR_BUILDING);

			int layer = -1; // just because ;)
			int maxLayer = 16;
			int brickHeight = gameHeight / (5 * maxLayer);
			int iglooWidth = gameWidth / 6;
			int iglooOffsetx = 0;
			int iglooOffsety = 0;
			for (; layer < Math.min(maxLayer, building) - 1; layer++) {
				if (layer == maxLayer / 3) {
					brickHeight /= 2;
					iglooOffsety = -(layer - 1) * brickHeight;
				}
				if (layer >= maxLayer / 3) {
					iglooWidth -= gameWidth / (4 * maxLayer);
					iglooOffsetx += gameWidth / (8 * maxLayer);
				}
				g2.fill(new Rectangle2D.Double(iglooOffsetx + 3 * gameWidth / 4,
						iglooOffsety + gameHeight / 5 - brickHeight * layer,
						iglooWidth, brickHeight));
			}
			if (building >= maxLayer) {
				g2.setColor(Color.black);
				int doorWidth = gameWidth / 28;
				int doorHeight = gameHeight / 20;
				g2.fill(new Rectangle2D.Double(3 * gameWidth / 4 + gameWidth / 12 - doorWidth / 2,
						gameHeight / 5 - doorHeight/2, doorWidth, doorHeight));
			}
		}
	}

}
