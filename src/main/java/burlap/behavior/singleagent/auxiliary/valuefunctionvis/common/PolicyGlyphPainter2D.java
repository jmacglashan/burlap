package burlap.behavior.singleagent.auxiliary.valuefunctionvis.common;

import burlap.behavior.policy.EnumerablePolicy;
import burlap.behavior.policy.Policy;
import burlap.behavior.policy.support.ActionProb;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.StatePolicyPainter;
import burlap.mdp.core.state.State;
import burlap.mdp.core.state.vardomain.VariableDomain;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
	protected VariableDomain xRange;


	/**
	 * Range of the y key
	 */
	protected VariableDomain yRange;

	/**
	 * Width of x cells
	 */
	protected double xWidth;


	/**
	 * width of y cells
	 */
	protected double yWidth;
	
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
	 * Sets the variable keys for the x and y variables in the state and the width of cells along those domains.
	 * The widths along the x dimension are how much of the variable space each rendered state will take.
	 * @param xKey the x variable key
	 * @param yKey the y variable key
	 * @param xRange the range of the x values
	 * @param yRange the range of the y values
	 * @param xWidth the width of a state along the x domain
	 * @param yWidth the width of a state alone the y domain
	 */
	public void setXYKeys(Object xKey, Object yKey, VariableDomain xRange, VariableDomain yRange, double xWidth, double yWidth){
		this.xKey = xKey;
		this.yKey = yKey;
		this.xRange = xRange;
		this.yRange = yRange;
		this.xWidth = xWidth;
		this.yWidth = yWidth;
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

		
		List<ActionProb> pdist;
		if(policy instanceof EnumerablePolicy) {
			pdist = ((EnumerablePolicy)policy).policyDistribution(s);
		}
		else{
			pdist = Arrays.asList(new ActionProb(policy.action(s), 1.));
		}
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

}
