package burlap.behavior.singleagent.auxiliary.valuefunctionvis.common;

import burlap.behavior.singleagent.auxiliary.valuefunctionvis.StateValuePainter;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.range.VariableRange;

import java.awt.*;
import java.awt.geom.Rectangle2D;


/**
 * A class for rendering the value of states as colored 2D cells on the canvas.
 * @author James MacGlashan
 *
 */
public class StateValuePainter2D extends StateValuePainter {


	/**
	 * variable key for the x variable
	 */
	protected Object xKey;


	/**
	 * variable key for the y variable
	 */
	protected Object yKey;


	/**
	 * Range of the x key
	 */
	protected VariableRange xRange;


	/**
	 * Range of the y key
	 */
	protected VariableRange yRange;

	/**
	 * Width of x cells
	 */
	protected double xWidth;


	/**
	 * width of y cells
	 */
	protected double yWidth;
	
	
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
	 * Sets the variable keys for the x and y variables in the state and the width of cells along those domains.
	 * The widths along the x dimension are how much of the variable space each rendered state will take.
	 * @param xKey the x variable key
	 * @param yKey the y variable key
	 * @param xRange the range of the x values
	 * @param yRange the range of the y values
	 * @param xWidth the width of a state along the x domain
	 * @param yWidth the width of a state alone the y domain
	 */
	public void setXYKeys(Object xKey, Object yKey, VariableRange xRange, VariableRange yRange, double xWidth, double yWidth){
		this.xKey = xKey;
		this.yKey = yKey;
		this.xRange = xRange;
		this.yRange = yRange;
		this.xWidth = xWidth;
		this.yWidth = yWidth;
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

		Number x = (Number)s.get(xKey);
		Number y = (Number)s.get(yKey);

		float xval;
		float yval;
		float width;
		float height;
		

		width = cWidth / (float)(xRange.span() / xWidth);
		height = cHeight / (float)(yRange.span() / yWidth);

		float normX = (float)xRange.norm(x.doubleValue());
		xval = normX * cWidth;


		float normY = (float)yRange.norm(y.doubleValue());
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


}
