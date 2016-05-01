package burlap.behavior.singleagent.auxiliary.valuefunctionvis.common;

import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.Policy.ActionProb;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.StatePolicyPainter;
import burlap.oomdp.core.objects.OldObjectInstance;
import burlap.oomdp.core.state.State;
import burlap.oomdp.core.Attribute.AttributeType;


/**
 * An class for rendering the policy for states by painting different glyphs for different actions. There are three different ways to paint the policy:
 * (1) just painting the glyph for the most likely action (and all that tie as the most likely); (2) glyphs for all actions whose likelihood is within
 * some threshold of the most likely action; (3) glyphs for all actions with the glyphs scaled by how likely the action is.
 * 
 *  
 * @author James MacGlashan
 *
 */
public class PolicyGlyphPainter2D implements StatePolicyPainter {

	
	/**
	 * MAXACTION paints only glphys for only those actions that have the highest likelihood <p>
	 * MAXACTIONSOFTTIE paints the glyphs for all actions whose likelihood is within some threshold of the most likely action <p>
	 * DISTSCALED paints glyphs for all actions and scales them by the likelihood of the action
	 * @author James MacGlashan
	 *
	 */
	public enum PolicyGlyphRenderStyle{
		
		MAXACTION(0),
		MAXACTIONSOFTTIE(1),
		DISTSCALED(2);
		
		private final int intVal;
		
		PolicyGlyphRenderStyle(int i) {
			this.intVal = i;
		}
		
		public int toInt(){
			return this.intVal;
		}

		
	}
	
	
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
	 * The render style to use
	 */
	protected PolicyGlyphRenderStyle 			renderStyle = PolicyGlyphRenderStyle.MAXACTIONSOFTTIE;
	
	/**
	 * The max probability difference from the most likely action for which an action that is not the most likely will still be rendered under the 
	 * MAXACTIONSOFTTIE rendering style.
	 */
	protected double							softTieDelta = 0.01;
	
	
	/**
	 * The map from action names to glyphs that will be used to represent them.
	 */
	protected Map<String, ActionGlyphPainter>	actionNameToGlyphPainter;
	
	
	public PolicyGlyphPainter2D(){
		this.actionNameToGlyphPainter = new HashMap<String, ActionGlyphPainter>();
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
	 * Sets which glyph painter to use for an action with the given name
	 * @param actionName the name of the action
	 * @param actionPainter the glyph painter used to represent it
	 */
	public void setActionNameGlyphPainter(String actionName, ActionGlyphPainter actionPainter){
		this.actionNameToGlyphPainter.put(actionName, actionPainter);
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

	
	
	/**
	 * Returns the rendering style
	 * @return the rendering style
	 */
	public PolicyGlyphRenderStyle getRenderStyle() {
		return renderStyle;
	}

	
	/**
	 * Sets the rendering style
	 * @param renderStyle the rending style to use
	 */
	public void setRenderStyle(PolicyGlyphRenderStyle renderStyle) {
		this.renderStyle = renderStyle;
	}

	/**
	 * Sets the soft difference between max actions to determine ties when the MAXACTIONSOFSOFTTIE render style is used.
	 * @param delta the delta for determining ties.
	 */
	public void setSoftTieRenderStyleDelta(double delta){
		this.softTieDelta = delta;
	}
	
	/**
	 * Returns the soft difference between max actions to determine ties when the MAXACTIONSOFSOFTTIE render style is used.
	 * @return the delta for determining ties.
	 */
	public double getSoftTieRenderStyleDelta(){
		return this.softTieDelta;
	}

	@Override
	public void paintStatePolicy(Graphics2D g2, State s, Policy policy, float cWidth, float cHeight) {
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
		
		if(this.numXCells != -1){
			domainXScale = this.numXCells;
		}
		else if(xAtt.type == Attribute.AttributeType.DISC){
			domainXScale = xAtt.discValues.size();
		}
		else if(xAtt.type == AttributeType.INT){
			domainXScale = (float)(xAtt.upperLim - xAtt.lowerLim + 1);
		}
		else {
			domainXScale = (float)(xAtt.upperLim - xAtt.lowerLim);
		}
		width = cWidth / domainXScale;
		xval = ((float)(xOb.getNumericValForAttribute(xAttName) - xAtt.lowerLim))*width;
		
		if(this.numYCells != -1){
			domainYScale = this.numYCells;
		}
		else if(yAtt.type == AttributeType.DISC){
			domainYScale = yAtt.discValues.size();
		}
		else if(yAtt.type == AttributeType.INT){
			domainYScale = (float)(yAtt.upperLim - yAtt.lowerLim + 1);
		}
		else{
			domainYScale = (float)(yAtt.upperLim - yAtt.lowerLim);
		}
		height = cHeight / domainYScale;
		yval = cHeight - height - ((float)(yOb.getNumericValForAttribute(yAttName) - yAtt.lowerLim))*height;
		
		
		List<ActionProb> pdist = policy.getActionDistributionForState(s);
		double maxp = 0.;
		for(ActionProb ap : pdist){
			if(ap.pSelection > maxp){
				maxp = ap.pSelection;
			}
			
		}
		
		if(this.renderStyle != PolicyGlyphRenderStyle.DISTSCALED){
			if(this.renderStyle == PolicyGlyphRenderStyle.MAXACTIONSOFTTIE){
				maxp -= this.softTieDelta;
			}
			
			for(ActionProb ap : pdist){
				if(ap.pSelection >= maxp){
					ActionGlyphPainter agp = this.actionNameToGlyphPainter.get(ap.ga.actionName());
					if(agp != null){
						agp.paintGlyph(g2, xval, yval, width, height);
					}
				}
			}
			
		}
		else{
			for(ActionProb ap : pdist){
				float [] scaledRect = this.rescaleRect(xval, yval, width, height, (float)(ap.pSelection/maxp));
				ActionGlyphPainter agp = this.actionNameToGlyphPainter.get(ap.ga.actionName());
				if(agp != null){
					agp.paintGlyph(g2, scaledRect[0], scaledRect[1], scaledRect[2], scaledRect[3]);
				}
			}
		}

	}
	
	
	/**
	 * Takes in a rectangle specification and scales it equally along each direction by a scale factor.
	 * @param x the left side of the rectangle
	 * @param y the top side of the rectangle
	 * @param width the width of the rectangle
	 * @param height the height of the rectangle
	 * @param scale the scale factor
	 * @return a double array with the new x, y, width, and height values of the scaled rectangle.
	 */
	protected float [] rescaleRect(float x, float y, float width, float height, float scale){
		
		float cx = x + (width/2f);
		float cy = y + (height/2f);
		
		float nw = scale*width;
		float nh = scale*height;
		
		float nx = cx - (nw/2f);
		float ny = cy - (nh/2f);
		
		return new float[]{nx,ny,nw,nh};
		
		
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
