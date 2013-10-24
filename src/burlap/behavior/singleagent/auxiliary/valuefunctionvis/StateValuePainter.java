package burlap.behavior.singleagent.auxiliary.valuefunctionvis;

import java.awt.Graphics2D;

import burlap.oomdp.core.State;

public abstract class StateValuePainter {

	protected boolean			shouldRescaleValues = true;
	
	
	/**
	 * @param g2 graphics context to which the object should be painted
	 * @param s the state of the object to be painted
	 * @param value the value function evaluation of state s
	 * @param cWidth width of the canvas size
	 * @param cHeight height of the canvas size
	 */
	public abstract void paintStateValue(Graphics2D g2, State s, double value, float cWidth, float cHeight);
	public abstract void rescale(double lowerValue, double upperValue);
	
	
	/**
	 * Enabling value rescaling allows the painter to adjust to the minimum and maximum values passed to it
	 * @param rescale whether this painter should rescale to the minimum and maximum value
	 */
	public void useValueRescaling(boolean rescale){
		this.shouldRescaleValues = rescale;
	}
	

	


}
