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


public class ValueFunctionVisualizer extends Canvas{


	private static final long serialVersionUID = 1L;
	
	
	protected Collection <State>				stateValuesToVisualize;

	protected StateValuePainter					svp;
	protected StatePolicyPainter				spp = null;
	protected StaticDomainPainter				sdp = null;
	
	protected Color								bgColor = Color.GRAY;					//the background color of the canvas
	
	protected Image								offscreen = null;
	protected Graphics2D						bufferedGraphics = null;
	
	protected QComputablePlanner				planner;
	protected Policy							policy;

	public ValueFunctionVisualizer(Collection <State> states, StateValuePainter svp, QComputablePlanner planner){
		this.stateValuesToVisualize = states;
		this.svp = svp;
		this.planner = planner;
	}

	public Collection<State> getStateValuesToVisualize() {
		return stateValuesToVisualize;
	}

	public void setStateValuesToVisualize(Collection<State> stateValuesToVisualize) {
		this.stateValuesToVisualize = stateValuesToVisualize;
	}

	public StateValuePainter getSvp() {
		return svp;
	}

	public void setSvp(StateValuePainter svp) {
		this.svp = svp;
	}

	public StatePolicyPainter getSpp() {
		return spp;
	}

	public void setSpp(StatePolicyPainter spp) {
		this.spp = spp;
	}

	public StaticDomainPainter getSdp() {
		return sdp;
	}

	public void setSdp(StaticDomainPainter sdp) {
		this.sdp = sdp;
	}
	
	

	public Policy getPolicy() {
		return policy;
	}

	public void setPolicy(Policy policy) {
		this.policy = policy;
	}

	public Color getBgColor() {
		return bgColor;
	}

	public void setBgColor(Color bgColor) {
		this.bgColor = bgColor;
	}
	
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
				this.spp.paintStateValue(bufferedGraphics, s, policy, cWidth, cHeight);
			}
		}
		
		
		Graphics2D g2 = (Graphics2D) g;
		g2.drawImage(offscreen,0,0,this);
		
		
	}
	
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
	
	 protected void initializeOffscreen(){
		 if(this.bufferedGraphics == null){
			 this.offscreen = createImage(this.getWidth(), this.getHeight());
			 this.bufferedGraphics = (Graphics2D)offscreen.getGraphics();
		 }
	 }
	

}
