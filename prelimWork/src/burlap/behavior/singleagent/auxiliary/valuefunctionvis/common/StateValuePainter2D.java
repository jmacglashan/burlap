package burlap.behavior.singleagent.auxiliary.valuefunctionvis.common;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import burlap.behavior.singleagent.auxiliary.valuefunctionvis.StateValuePainter;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;


/**
 * A class for rendering the value of states as colored 2D cells on the canvas.
 * @author James MacGlashan
 *
 */
public class StateValuePainter2D extends StateValuePainter {

	
	/**
	 * The name of the attribute that is used for determining the x-position on the canvas
	 */
	protected String							xAttName;
	
	/**
	 * The name of the attribute that is used for determining the y-position on the canvas
	 */
	protected String							yAttName;
	
	
	/**
	 * The name of the class that holds the x-attribute used for determining the x-posiition on the canvas
	 */
	protected String							xClassName;
	
	/**
	 * The name of the class that holds the y-attribute used for determining the y-posiition on the canvas
	 */
	protected String							yClassName;
	
	
	/**
	 * The name of the object that holds the x-attribute used for determining the x-posiition on the canvas
	 */
	protected String							xObjectName;
	
	/**
	 * The name of the object that holds the y-attribute used for determining the y-posiition on the canvas
	 */
	protected String							yObjectName;
	
	
	/**
	 * The object to use for returning the color with which to fill the state cell given its value.
	 */
	protected ColorBlend						colorBlend;
	
	/**
	 * Option used for specifying the number of possible states that will be rendered in a row (i.e., across the x-axis). -1 causes the
	 * attribute categorical size to be used instead.
	 */
	protected int								numXCells = -1;
	
	/**
	 * Option used for specifying the number of possible states that will be rendered in a column (i.e., across the y-axis). -1 causes the
	 * attribute categorical size to be used instead.
	 */
	protected int								numYCells = -1;
	
	
	/**
	 * Initializes the value painter.
	 * @param colorBlend the object to use for returning the color with which to fill the state cell given its value.
	 */
	public StateValuePainter2D(ColorBlend colorBlend){
		this.colorBlend = colorBlend;
	}
	
	
	/**
	 * Will set the x-y attributes to use for cell rendering to the x y attributes of the first object in the state of the designated classes.
	 * @param xClassName the object class name containing the render x-axis attribute
	 * @param xAttName the render x-axis attribute name
	 * @param yClassName the object class name containing the render y-axis attribute
	 * @param yAttName the render y-axis attribute name
	 */
	public void setXYAttByObjectClass(String xClassName, String xAttName, String yClassName, String yAttName){
		this.xClassName = xClassName;
		this.xAttName = xAttName;
		
		this.yClassName = yClassName;
		this.yAttName = yAttName;
		
		this.xObjectName = null;
		this.yObjectName = null;
	}
	
	
	/**
	 * Will set the x-y attributes to use for cell rendering to the x y attributes of the designated object references.
	 * @param xObjectName the object name reference that contains the render x-axis attribute
	 * @param xAttName the render x-axis attribute name
	 * @param yObjectName the object name reference that contains the render y-axis attribute
	 * @param yAttName the render y-axis attribute name
	 */
	public void setXYAttByObjectReference(String xObjectName, String xAttName, String yObjectName, String yAttName){
		this.xObjectName = xObjectName;
		this.xAttName = xAttName;
		
		this.yObjectName = yObjectName;
		this.yAttName = yAttName;
		
		this.xClassName = null;
		this.yClassName = null;
	}

	
	
	/**
	 * Sets the number of states that will be rendered along a row
	 * @param numXCells the number of states that will be rendered along a row
	 */
	public void setNumXCells(int numXCells) {
		this.numXCells = numXCells;
	}


	
	/**
	 * Sets the number of states that will be rendered along a row
	 * @param numYCells the number of states that will be rendered along a column
	 */
	public void setNumYCells(int numYCells) {
		this.numYCells = numYCells;
	}

	
	@Override
	public void rescale(double lowerValue, double upperValue) {
		if(!this.shouldRescaleValues){
			return ;
		}
		this.colorBlend.rescale(lowerValue, upperValue);
		
	}
	

	@Override
	public void paintStateValue(Graphics2D g2, State s, double value, float cWidth, float cHeight) {
		
		ObjectInstance xOb = this.xObjectInstance(s);
		ObjectInstance yOb = this.yObjectInstance(s);
		
		Attribute xAtt = xOb.getObjectClass().getAttribute(xAttName);
		Attribute yAtt = yOb.getObjectClass().getAttribute(yAttName);
		
		float domainXScale = 0f;
		float domainYScale = 0f;
		float xval = 0f;
		float yval = 0f;
		float width = 0f;
		float height = 0f;
		
		if(xAtt.type == Attribute.AttributeType.DISC){
			
			if(this.numXCells != -1){
				domainXScale = this.numXCells;
			}
			else{
				domainXScale = xAtt.discValues.size();
			}
			
			width = cWidth / domainXScale;
			xval = xOb.getDiscValForAttribute(xAttName)*width;
			
		}
		
		if(yAtt.type == Attribute.AttributeType.DISC){
			
			if(this.numYCells != -1){
				domainYScale = this.numYCells;
			}
			else{
				domainYScale = yAtt.discValues.size();
			}
			
			height = cHeight / domainYScale;
			yval = cHeight - height - yOb.getDiscValForAttribute(yAttName)*height;
			
		}
		
		Color col = this.colorBlend.color(value);
		g2.setColor(col);
		
		g2.fill(new Rectangle2D.Float(xval, yval, width, height));
		

	}
	
	
	/**
	 * Returns the object instance in a state that holds the x-position information.
	 * @param s the state for which to get the x-position
	 * @return the object instance in a state that holds the x-position information.
	 */
	protected ObjectInstance xObjectInstance(State s){
		if(this.xClassName != null){
			return s.getFirstObjectOfClass(xClassName);
		}
		return s.getObject(xObjectName);
	}
	
	
	/**
	 * Returns the object instance in a state that holds the y-position information.
	 * @param s the state for which to get the y-position
	 * @return the object instance in a state that holds the y-position information.
	 */
	protected ObjectInstance yObjectInstance(State s){
		if(this.yClassName != null){
			return s.getFirstObjectOfClass(yClassName);
		}
		return s.getObject(yObjectName);
	}


	
	

}
