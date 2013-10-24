package burlap.behavior.singleagent.auxiliary.valuefunctionvis.common;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import burlap.behavior.singleagent.auxiliary.valuefunctionvis.StateValuePainter;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

public class StateValuePainter2D extends StateValuePainter {

	
	protected String				xAttName;
	protected String				yAttName;
	
	protected String				xClassName;
	protected String				yClassName;
	
	protected String				xObjectName;
	protected String				yObjectName;
	
	protected ColorBlend			colorBlend;
	
	protected int					numXCells = -1;
	protected int					numYCells = -1;
	
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

	
	public void setNumXCells(int numXCells) {
		this.numXCells = numXCells;
	}


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
	
	
	protected ObjectInstance xObjectInstance(State s){
		if(this.xClassName != null){
			return s.getFirstObjectOfClass(xClassName);
		}
		return s.getObject(xObjectName);
	}
	
	protected ObjectInstance yObjectInstance(State s){
		if(this.yClassName != null){
			return s.getFirstObjectOfClass(yClassName);
		}
		return s.getObject(yObjectName);
	}


	
	

}
