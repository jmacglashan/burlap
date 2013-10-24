package burlap.behavior.singleagent.auxiliary.valuefunctionvis.common;

import java.awt.Color;

public interface ColorBlend {
	public Color color(double v);
	public void rescale(double minV, double maxV);
}
