package burlap.behavior.singleagent.auxiliary.valuefunctionvis.common;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import burlap.behavior.singleagent.auxiliary.valuefunctionvis.StateValuePainter;
import burlap.oomdp.core.objects.OldObjectInstance;
import burlap.oomdp.core.state.State;
import burlap.oomdp.core.Attribute.AttributeType;


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
	 * The name of the class that holds the x-attribute used for determining the x-position on the canvas
	 */
	protected String							xClassName;
	
	/**
	 * The name of the class that holds the y-attribute used for determining the y-position on the canvas
	 */
	protected String							yClassName;
	
	
	/**
	 * The name of the object that holds the x-attribute used for determining the x-position on the canvas
	 */
	protected String							xObjectName;
	
	/**
	 * The name of the object that holds the y-attribute used for determining the y-position on the canvas
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
	
	
	//TODO: setters for the below
	
	/**
	 * Whether the numeric string for the value of the state should be rendered in its cell or not.
	 */
	protected boolean							renderValueString = true;
	
	/**
	 * The font point size of the value string
	 */
	protected int								vsFontSize = 10;
	
	/**
	 * The font color of the value strings
	 */
	protected Color								vsFontColor = Color.BLACK;
	
	/**
	 * A value between 0 and 1 indicating how far from the left of a value cell the value string should start being rendered.
	 * 0 indicates stating at the left of the cell; 1 the right;
	 */
	protected float								vsOffsetFromLeft = 0f;
	
	/**
	 * A value between 0 and 1 indicating how from from the top of a value cell the value string should start be rendered.
	 * 0 indicates stating at the top of the cell; 1 the bottom;
	 */
	protected float								vsOffsetFromTop = 0.75f;
	
	/**
	 * The precision (number of decimals) shown in the value string.
	 */
	protected int								vsPrecision = 2;


	/**
	 * Initializes using a {@link burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.LandmarkColorBlendInterpolation}
	 * object that mixes from red (lowest value) to blue (highest value).
	 */
	public StateValuePainter2D(){
		LandmarkColorBlendInterpolation rb = new LandmarkColorBlendInterpolation();
		rb.addNextLandMark(0., Color.RED);
		rb.addNextLandMark(1., Color.BLUE);
		this.colorBlend = rb;
	}


	/**
	 * Initializes the value painter.
	 * @param colorBlend the object to use for returning the color with which to fill the state cell given its value.
	 */
	public StateValuePainter2D(ColorBlend colorBlend){
		this.colorBlend = colorBlend;
	}


	/**
	 * Sets the color blending used for the value function.
	 * @param colorBlend the color blending used for the value function.
	 */
	public void setColorBlend(ColorBlend colorBlend){
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
	 * Enables or disables the rendering the text specifying the value of a state in its cell.
	 * @param renderValueString if true, then text specifying the value of the state will be rendered; if false then it will not be rendered.
	 */
	public void toggleValueStringRendering(boolean renderValueString){
		this.renderValueString = renderValueString;
	}
	
	
	/**
	 * Sets the rendering format of the string displaying the value of each state.
	 * @param fontSize the font size of the string
	 * @param fontColor the color of the font
	 * @param precision the precision of the value text printed (e.g., 2 means displaying 2 decimal places)
	 * @param offsetFromLeft the offset from the left side of a state's cell that the text will begin being rendered. 0 means starting on the left boundary, 1 on the right boundary.
	 * @param offsetFromTop the offset from the top side of a state's cell that the text will begin being rendered. 0 means starting on the top boundary, 1 on the bottom boundary.
	 */
	public void setValueStringRenderingFormat(int fontSize, Color fontColor, int precision, float offsetFromLeft, float offsetFromTop){
		this.vsFontSize = fontSize;
		this.vsFontColor = fontColor;
		this.vsPrecision = precision;
		this.vsOffsetFromLeft = offsetFromLeft;
		this.vsOffsetFromTop = offsetFromTop;
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
		
		OldObjectInstance xOb = this.xObjectInstance(s);
		OldObjectInstance yOb = this.yObjectInstance(s);
		
		Attribute xAtt = xOb.getObjectClass().getAttribute(xAttName);
		Attribute yAtt = yOb.getObjectClass().getAttribute(yAttName);
		
		float domainXScale;
		float domainYScale;
		float xval;
		float yval;
		float width;
		float height;
		

		if(xAtt.type == Attribute.AttributeType.DISC){
			domainXScale = xAtt.discValues.size();
		}
		else if(xAtt.type == AttributeType.INT){
			domainXScale = (float)(xAtt.upperLim - xAtt.lowerLim + 1);
		}
		else {
			domainXScale = (float)(xAtt.upperLim - xAtt.lowerLim);
		}

		if(this.numXCells != -1){
			width = cWidth / this.numXCells;
		}
		else{
			width = cWidth / domainXScale;
		}

		float normX = (float)(xOb.getNumericValForAttribute(xAttName) - xAtt.lowerLim) / domainXScale;
		xval = normX * cWidth;
		

		if(yAtt.type == AttributeType.DISC){
			domainYScale = yAtt.discValues.size();
		}
		else if(yAtt.type == AttributeType.INT){
			domainYScale = (float)(yAtt.upperLim - yAtt.lowerLim + 1);
		}
		else{
			domainYScale = (float)(yAtt.upperLim - yAtt.lowerLim);
		}

		if(this.numYCells != -1){
			height = cHeight / this.numYCells;
		}
		else{
			height = cHeight / domainYScale;
		}

		float normY = (float)(yOb.getNumericValForAttribute(yAttName) - yAtt.lowerLim) / domainYScale;
		yval = cHeight - height - normY*cHeight;
		
		
		
		Color col = this.colorBlend.color(value);
		g2.setColor(col);
		
		g2.fill(new Rectangle2D.Float(xval, yval, width, height));
		
		if(this.renderValueString){
			
			g2.setColor(this.vsFontColor);
			g2.setFont(new Font("sansserif", Font.BOLD, this.vsFontSize));
			String fstring = String.format("%."+this.vsPrecision+"f", value);
			
			float sxval = xval + this.vsOffsetFromLeft*width;
			float syval = yval + this.vsOffsetFromTop*height;
			
			g2.drawString(fstring, sxval, syval);
			
		}
		

	}
	
	
	/**
	 * Returns the object instance in a state that holds the x-position information.
	 * @param s the state for which to get the x-position
	 * @return the object instance in a state that holds the x-position information.
	 */
	protected OldObjectInstance xObjectInstance(State s){
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
	protected OldObjectInstance yObjectInstance(State s){
		if(this.yClassName != null){
			return s.getFirstObjectOfClass(yClassName);
		}
		return s.getObject(yObjectName);
	}


	
	

}
