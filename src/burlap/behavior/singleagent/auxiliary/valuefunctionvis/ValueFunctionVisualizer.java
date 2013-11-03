package burlap.behavior.singleagent.auxiliary.valuefunctionvis;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.oomdp.core.State;


/**
 * Used to visualize the value function for a collection of states. This class makes use of the {@link StateValuePainter} {@link StatePolicyPainter}
 * and {@link StaticDomainPainter} to pain the value function. Each state is iterated over and the the respective classes are used to pain its value function/policy.
 * The visualizer requires a QComputablePlanner to retrieve the value function. If no policy is given, it will not be rendered.
 * @author James MacGlashan
 *
 */
public class ValueFunctionVisualizer extends Canvas{


	private static final long serialVersionUID = 1L;
	
	/**
	 * The states to visualize
	 */
	protected Collection <State>				stateValuesToVisualize;

	
	/**
	 * Painter used to visualize the value function
	 */
	protected StateValuePainter					svp;
	
	/**
	 * Painter used to visualize the policy
	 */
	protected StatePolicyPainter				spp = null;
	
	/**
	 * Painter used to visualize general state-independent domain information
	 */
	protected StaticDomainPainter				sdp = null;
	
	/**
	 * The background color of the canvas
	 */
	protected Color								bgColor = Color.GRAY;					//the background color of the canvas
	
	/**
	 * Offscreen image to render to first
	 */
	protected Image								offscreen = null;
	
	/**
	 * The graphics context of the offscreen image
	 */
	protected Graphics2D						bufferedGraphics = null;
	
	/**
	 * The QComputable planner to use for finding the value function
	 */
	protected QComputablePlanner				planner;
	
	/**
	 * The policy to use for visualizing the policy
	 */
	protected Policy							policy;

	
	
	/**
	 * Initializes the visualizer.
	 * @param states the states whose value should be rendered.
	 * @param svp the value function state visualizer to use.
	 * @param planner the planner that can return the value function.
	 */
	public ValueFunctionVisualizer(Collection <State> states, StateValuePainter svp, QComputablePlanner planner){
		this.stateValuesToVisualize = states;
		this.svp = svp;
		this.planner = planner;
	}

	/**
	 * Returns the states that will be visualized
	 * @return the states that will be visualized
	 */
	public Collection<State> getStateValuesToVisualize() {
		return stateValuesToVisualize;
	}

	
	/**
	 * Sets the states to visualize
	 * @param stateValuesToVisualize the state to visualize
	 */
	public void setStateValuesToVisualize(Collection<State> stateValuesToVisualize) {
		this.stateValuesToVisualize = stateValuesToVisualize;
	}

	/**
	 * Returns the State-wise value function painter
	 * @return the State-wise value function painter
	 */
	public StateValuePainter getSvp() {
		return svp;
	}

	
	/**
	 * Sets the state-wise value function painter
	 * @param svp state-wise value function painter
	 */
	public void setSvp(StateValuePainter svp) {
		this.svp = svp;
	}

	/**
	 * Returns the state-wise policy painter
	 * @return the state-wise policy painter
	 */
	public StatePolicyPainter getSpp() {
		return spp;
	}

	
	/**
	 * Sets the state-wise policy painter
	 * @param spp the state-wise policy painter
	 */
	public void setSpp(StatePolicyPainter spp) {
		this.spp = spp;
	}

	/**
	 * Returns the state-independent domain painter
	 * @return the state-independent domain painter
	 */
	public StaticDomainPainter getSdp() {
		return sdp;
	}

	
	/**
	 * Sets the state-independent domain painter
	 * @param sdp the state-independent domain painter
	 */
	public void setSdp(StaticDomainPainter sdp) {
		this.sdp = sdp;
	}
	
	
	/**
	 * Returns the policy that will be rendered.
	 * @return the policy to be rendered
	 */
	public Policy getPolicy() {
		return policy;
	}

	
	/**
	 * Sets the policy to render
	 * @param policy the policy to render
	 */
	public void setPolicy(Policy policy) {
		this.policy = policy;
	}

	
	/**
	 * Returns the canvas background color
	 * @return the canvas background color
	 */
	public Color getBgColor() {
		return bgColor;
	}

	
	/**
	 * Sets the canvas background color
	 * @param bgColor the canvas background color
	 */
	public void setBgColor(Color bgColor) {
		this.bgColor = bgColor;
	}
	
	
	@Override
	public void paint(Graphics g){
		
		this.initializeOffscreen();
		
		float cWidth = this.getWidth();
		float cHeight = this.getHeight();
		
		this.bufferedGraphics.setColor(bgColor);
		this.bufferedGraphics.fill(new Rectangle((int)cWidth, (int)cHeight));
		
		
		if(this.sdp != null){
			this.sdp.paint(bufferedGraphics, cWidth, cHeight);
		}
		
		List <Double> values = new ArrayList<Double>(this.stateValuesToVisualize.size());
		double minV = Double.POSITIVE_INFINITY;
		double maxV = Double.NEGATIVE_INFINITY;
		for(State s : this.stateValuesToVisualize){
			double v = this.getVValue(s);
			values.add(v);
			if(v < minV){
				minV = v;
			}
			if(v > maxV){
				maxV = v;
			}
		}
		
		this.svp.rescale(minV, maxV);
		
		Iterator<Double> vIter = values.iterator();
		for(State s : this.stateValuesToVisualize){
			this.svp.paintStateValue(bufferedGraphics, s, vIter.next(), cWidth, cHeight);
		}
		
		if(this.spp != null){
			for(State s : this.stateValuesToVisualize){
				this.spp.paintStatePolicy(bufferedGraphics, s, policy, cWidth, cHeight);
			}
		}
		
		
		Graphics2D g2 = (Graphics2D) g;
		g2.drawImage(offscreen,0,0,this);
		
		
	}
	
	/**
	 * Returns the value for a state
	 * @param s the state for which to get the value
	 * @return the value for a state
	 */
	protected double getVValue(State s){
		List <QValue> qs = this.planner.getQs(s);
		double max = Double.NEGATIVE_INFINITY;
		for(QValue q : qs){
			if(q.q > max){
				max = q.q;
			}
		}
		return max;
	}
	
	
	/**
	 * Initializes a new offscreen image and context
	 */
	 protected void initializeOffscreen(){
		 if(this.bufferedGraphics == null){
			 this.offscreen = createImage(this.getWidth(), this.getHeight());
			 this.bufferedGraphics = (Graphics2D)offscreen.getGraphics();
		 }
	 }
	

}
