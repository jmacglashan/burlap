package burlap.domain.singleagent.frostbite;

import burlap.oomdp.core.objects.OldObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.visualizer.ObjectPainter;
import burlap.oomdp.visualizer.StaticPainter;
import burlap.oomdp.visualizer.Visualizer;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * @author Phillipe Morere
 */
public class FrostbiteVisualizer {

	private final static Color activatedPlatformColor = new Color(0.73333335f, 0.84313726f, 0.9411765f);
	private final static Color iglooColor = new Color(0.7294118f, 0.827451f, 0.84313726f);
	private final static Color waterColor = new Color(0.34509805f, 0.43529412f, 0.60784316f);
	static int glob = 0;

	private FrostbiteVisualizer() {
	    // do nothing
	}
	
	/**
	 * Returns a visualizer for a lunar lander domain.
	 *
	 * @param fd the specific frostbite domain generator to visualize
	 * @return a visualizer for a lunar lander domain.
	 */
	public static Visualizer getVisualizer(FrostbiteDomain fd) {

		Visualizer v = new Visualizer();

		v.addStaticPainter(new StaticPainter() {
			@Override
			public void paint(Graphics2D g2, State s, float cWidth, float cHeight) {
				g2.setColor(waterColor);
				g2.fill(new Rectangle2D.Double(0, 0, FrostbiteDomain.gameWidth, FrostbiteDomain.gameHeight));
				g2.setColor(Color.white);
				g2.fill(new Rectangle2D.Double(0, 0, FrostbiteDomain.gameWidth, FrostbiteDomain.gameIceHeight));
			}
		});

		v.addObjectClassPainter(FrostbiteDomain.PLATFORMCLASS, new PlatformPainter(fd));
		v.addObjectClassPainter(FrostbiteDomain.IGLOOCLASS, new IglooPainter(fd));
		v.addObjectClassPainter(FrostbiteDomain.AGENTCLASS, new AgentPainter(fd));

		return v;
	}

	public static class PlatformPainter implements ObjectPainter {

		protected FrostbiteDomain fd;

		public PlatformPainter(FrostbiteDomain fd) {
			this.fd = fd;
		}

		@Override
		public void paintObject(Graphics2D g2, State s, OldObjectInstance ob,
								float cWidth, float cHeight) {


			int x = ob.getIntValForAttribute(FrostbiteDomain.XATTNAME);
			int y = ob.getIntValForAttribute(FrostbiteDomain.YATTNAME);
			int size = ob.getIntValForAttribute(FrostbiteDomain.SIZEATTNAME);
			boolean activated = ob.getBooleanValForAttribute(FrostbiteDomain.ACTIVATEDATTNAME);
			if (activated)
				g2.setColor(activatedPlatformColor);
			else
				g2.setColor(Color.white);

			g2.fill(new Rectangle2D.Double(x, y, size, size));

			if (x + size > FrostbiteDomain.gameWidth)
				g2.fill(new Rectangle2D.Double(x - FrostbiteDomain.gameWidth, y, size, size));
			else if (x < 0)
				g2.fill(new Rectangle2D.Double(x + FrostbiteDomain.gameWidth, y, size, size));
		}
	}

	public static class AgentPainter implements ObjectPainter {

		protected FrostbiteDomain fd;

		public AgentPainter(FrostbiteDomain fd) {
			this.fd = fd;
		}

		@Override
		public void paintObject(Graphics2D g2, State s, OldObjectInstance ob,
								float cWidth, float cHeight) {

			g2.setColor(Color.black);

			int x = ob.getIntValForAttribute(FrostbiteDomain.XATTNAME);
			int y = ob.getIntValForAttribute(FrostbiteDomain.YATTNAME);
			int size = fd.getAgentSize();

			g2.fill(new Rectangle2D.Double(x, y, size, size));
		}
	}

	public static class IglooPainter implements ObjectPainter {

		protected FrostbiteDomain fd;

		public IglooPainter(FrostbiteDomain fd) {
			this.fd = fd;
		}

		@Override
		public void paintObject(Graphics2D g2, State s, OldObjectInstance ob,
								float cWidth, float cHeight) {

			g2.setColor(iglooColor);

			int building = ob.getIntValForAttribute(FrostbiteDomain.BUILDINGATTNAME);

			int layer = -1; // just because ;)
			int maxLayer = fd.buildingStepsToWin;
			int brickHeight = FrostbiteDomain.gameHeight / (5 * maxLayer);
			int iglooWidth = FrostbiteDomain.gameWidth / 6;
			int iglooOffsetx = 0;
			int iglooOffsety = 0;
			for (; layer < Math.min(maxLayer, building) - 1; layer++) {
				if (layer == maxLayer / 3) {
					brickHeight /= 2;
					iglooOffsety = -(layer - 1) * brickHeight;
				}
				if (layer >= maxLayer / 3) {
					iglooWidth -= FrostbiteDomain.gameWidth / (4 * maxLayer);
					iglooOffsetx += FrostbiteDomain.gameWidth / (8 * maxLayer);
				}
				g2.fill(new Rectangle2D.Double(iglooOffsetx + 3 * FrostbiteDomain.gameWidth / 4,
						iglooOffsety + FrostbiteDomain.gameHeight / 5 - brickHeight * layer,
						iglooWidth, brickHeight));
			}
			if (building >= maxLayer) {
				g2.setColor(Color.black);
				int doorWidth = FrostbiteDomain.gameWidth / 28;
				int doorHeight = FrostbiteDomain.gameHeight / 20;
				g2.fill(new Rectangle2D.Double(3 * FrostbiteDomain.gameWidth / 4 + FrostbiteDomain.gameWidth / 12 - doorWidth / 2,
						FrostbiteDomain.gameHeight / 5 - doorHeight/2, doorWidth, doorHeight));
			}
		}
	}

}
