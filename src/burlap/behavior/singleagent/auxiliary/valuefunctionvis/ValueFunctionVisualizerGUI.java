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


/**
 * Provides a GUI for a value function and policy visualizer. Provides a toggle button for rendering the policy if the policy is provided.
 * @author James MacGlashan
 *
 */
public class ValueFunctionVisualizerGUI extends JFrame implements ItemListener {

	private static final long serialVersionUID = 1L;
	
	
	/**
	 * The visualizer to use.
	 */
	protected ValueFunctionVisualizer			visualizer;
	
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
	 * The button to enable the visualization of the policy
	 */
	protected JCheckBox							showPolicy;
	
	
	/**
	 * Visualizer canvas width
	 */
	protected int								cWidth = 800;
	
	/**
	 * Visualizer canvas height
	 */
	protected int								cHeight = 800;
	
	
	/**
	 * Initializes the visualizer GUI.
	 * @param states the states whose value should be rendered.
	 * @param svp the value function state visualizer to use.
	 * @param planner the planner that can return the value function.
	 */
	public ValueFunctionVisualizerGUI(List <State> states, StateValuePainter svp, QComputablePlanner planner){
		this.visualizer = new ValueFunctionVisualizer(states, svp, planner);
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
		this.visualizer.setSvp(svp);
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
		if(spp != null && this.showPolicy != null){
			this.showPolicy.setEnabled(true);
		}
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
		this.visualizer.setSdp(sdp);
	}
	
	
	/**
	 * Sets the canvas background color
	 * @param bgColor the canvas background color
	 */
	public void setBgColor(Color col){
		this.visualizer.setBgColor(col);
	}
	
	
	/**
	 * Sets the policy to render
	 * @param policy the policy to render
	 */
	public void setPolicy(Policy p){
		this.visualizer.setPolicy(p);
	}
	
	
	/**
	 * Initializes the GUI and presents it to the user.
	 */
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

	
	/**
	 * Called when the check back for the policy rendering is checked or unchecked.
	 */
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
