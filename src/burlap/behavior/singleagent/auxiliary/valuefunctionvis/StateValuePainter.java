package burlap.behavior.singleagent.auxiliary.valuefunctionvis;

import java.awt.Graphics2D;

import burlap.oomdp.core.State;


/**
 * An abstract class for defining the interface and common methods to paint the representation of the value function for a specific state onto
 * a 2D graphics context.
 * @author James MacGlashan
 *
 */
public abstract class StateValuePainter {

	/**
	 * Indicates whether this painter should scale its rendering of values to whatever it is told the minimum and maximum values are.
	 */
	protected boolean			shouldRescaleValues = true;
	
	
	/**
	 * Paints the representation of a value function for a specific state.
	 * @param g2 graphics context to which the object should be painted
	 * @param s the state of the object to be painted
	 * @param value the value function evaluation of state s
	 * @param cWidth width of the canvas size
	 * @param cHeight height of the canvas size
	 */
	public abstract void paintStateValue(Graphics2D g2, State s, double value, float cWidth, float cHeight);
	
	/**
	 * Used to tell this painter that it should render state values so that the minimum possible value is lowerValue and the maximum is upperValue.
	 * @param lowerValue the minimum value of state values
	 * @param upperValue the maximium value of state values
	 */
	public abstract void rescale(double lowerValue, double upperValue);
	
	
	/**
	 * Enabling value rescaling allows the painter to adjust to the minimum and maximum values passed to it.
	 * @param rescale whether this painter should rescale to the minimum and maximum value of the value function.
	 */
	public void useValueRescaling(boolean rescale){
		this.shouldRescaleValues = rescale;
	}
	

	


}
