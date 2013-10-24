package burlap.behavior.singleagent.auxiliary.valuefunctionvis;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.List;

import javax.swing.JFrame;

import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.oomdp.core.State;

public class ValueFunctionVisualizerGUI extends JFrame {

	private static final long serialVersionUID = 1L;
	
	protected ValueFunctionVisualizer			visualizer;
	protected StateValuePainter					svp;
	protected StatePolicyPainter				spp = null;
	protected StaticDomainPainter				sdp = null;
	
	protected int								cWidth = 800;
	protected int								cHeight = 800;
	
	public ValueFunctionVisualizerGUI(List <State> states, StateValuePainter svp, QComputablePlanner planner){
		this.visualizer = new ValueFunctionVisualizer(states, svp, planner);
	}

	public StateValuePainter getSvp() {
		return svp;
	}

	public void setSvp(StateValuePainter svp) {
		this.svp = svp;
		this.visualizer.setSvp(svp);
	}

	public StatePolicyPainter getSpp() {
		return spp;
	}

	public void setSpp(StatePolicyPainter spp) {
		this.spp = spp;
		this.visualizer.setSpp(spp);
	}

	public StaticDomainPainter getSdp() {
		return sdp;
	}

	public void setSdp(StaticDomainPainter sdp) {
		this.sdp = sdp;
		this.visualizer.setSdp(sdp);
	}
	
	public void setBgColor(Color col){
		this.visualizer.setBgColor(col);
	}
	
	
	public void initGUI(){
		
		this.visualizer.setPreferredSize(new Dimension(cWidth, cHeight));
		
		this.getContentPane().add(visualizer, BorderLayout.CENTER);
		
		pack();
		setVisible(true);
		
		this.visualizer.repaint();
		
	}

	
	
}
