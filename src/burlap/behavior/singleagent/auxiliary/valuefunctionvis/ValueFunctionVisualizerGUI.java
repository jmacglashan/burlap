package burlap.behavior.singleagent.auxiliary.valuefunctionvis;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JFrame;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.oomdp.core.State;

public class ValueFunctionVisualizerGUI extends JFrame implements ItemListener {

	private static final long serialVersionUID = 1L;
	
	protected ValueFunctionVisualizer			visualizer;
	protected StateValuePainter					svp;
	protected StatePolicyPainter				spp = null;
	protected StaticDomainPainter				sdp = null;
	
	protected JCheckBox							showPolicy;
	
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
		if(spp != null && this.showPolicy != null){
			this.showPolicy.setEnabled(true);
		}
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
	
	public void setPolicy(Policy p){
		this.visualizer.setPolicy(p);
	}
	
	
	public void initGUI(){
		
		this.visualizer.setPreferredSize(new Dimension(cWidth, cHeight));
		
		this.getContentPane().add(visualizer, BorderLayout.CENTER);
		
		Container controlContainer = new Container();
		controlContainer.setLayout(new BorderLayout());
		
		this.showPolicy = new JCheckBox("Show Policy");
		this.showPolicy.setSelected(false);
		this.showPolicy.addItemListener(this);
		if(this.spp == null){
			this.showPolicy.setEnabled(false);
		}
		
		controlContainer.add(this.showPolicy, BorderLayout.WEST);
		
		this.getContentPane().add(controlContainer, BorderLayout.SOUTH);
		
		pack();
		setVisible(true);
		
		this.visualizer.repaint();
		
	}

	
	
	public void itemStateChanged(ItemEvent e) {

	    Object source = e.getItemSelectable();
	    if(source == this.showPolicy){
	    	if(this.showPolicy.isSelected()){
	    		this.visualizer.setSpp(this.spp);
	    	}
	    	else{
	    		this.visualizer.setSpp(null);
	    	}
	    	this.visualizer.repaint();
	    }
	    
	}
	
	
}
