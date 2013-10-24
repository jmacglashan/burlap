package burlap.behavior.singleagent.auxiliary.valuefunctionvis.common;

import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.Policy.ActionProb;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.StatePolicyPainter;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

public class PolicyGlyphPainter2D implements StatePolicyPainter {

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
	
	
	protected String							xAttName;
	protected String							yAttName;
	
	protected String							xClassName;
	protected String							yClassName;
	
	protected String							xObjectName;
	protected String							yObjectName;
	
	protected int								numXCells = -1;
	protected int								numYCells = -1;
	
	protected PolicyGlyphRenderStyle 			renderStyle = PolicyGlyphRenderStyle.MAXACTIONSOFTTIE;
	protected double							softTieDelta = 0.01;
	
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

	
	public void setActionNameGlyphPainter(String actionName, ActionGlyphPainter actionPainter){
		this.actionNameToGlyphPainter.put(actionName, actionPainter);
	}
	
	public void setNumXCells(int numXCells) {
		this.numXCells = numXCells;
	}


	public void setNumYCells(int numYCells) {
		this.numYCells = numYCells;
	}

	
	
	
	public PolicyGlyphRenderStyle getRenderStyle() {
		return renderStyle;
	}

	public void setRenderStyle(PolicyGlyphRenderStyle renderStyle) {
		this.renderStyle = renderStyle;
	}


	@Override
	public void paintStateValue(Graphics2D g2, State s, Policy policy, float cWidth, float cHeight) {
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
					ActionGlyphPainter agp = this.actionNameToGlyphPainter.get(ap.ga.action.getName());
					agp.paintGlyph(g2, xval, yval, width, height);
				}
			}
			
		}
		else{
			for(ActionProb ap : pdist){
				float [] scaledRect = this.rescaleRect(xval, yval, width, height, (float)(ap.pSelection/maxp));
				ActionGlyphPainter agp = this.actionNameToGlyphPainter.get(ap.ga.action.getName());
				agp.paintGlyph(g2, scaledRect[0], scaledRect[1], scaledRect[2], scaledRect[3]);
			}
		}

	}
	
	
	protected float [] rescaleRect(float x, float y, float width, float height, float scale){
		
		float cx = x + (width/2f);
		float cy = y + (height/2f);
		
		float nw = scale*width;
		float nh = scale*height;
		
		float nx = cx - (nw/2f);
		float ny = cy - (nh/2f);
		
		return new float[]{nx,ny,nw,nh};
		
		
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
