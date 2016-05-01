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

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.ArrowActionGlyph;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.PolicyGlyphPainter2D;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.StateValuePainter2D;
import burlap.behavior.valuefunction.ValueFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.visualizer.MultiLayerRenderer;


/**
 * Provides a GUI for a value function and policy visualizer. Provides a toggle button for rendering the policy if the policy is provided.
 * @author James MacGlashan
 *
 */
public class ValueFunctionVisualizerGUI extends JFrame implements ItemListener {

	private static final long serialVersionUID = 1L;
	
	
	/**
	 * The multi-layer render layer canvas
	 */
	protected MultiLayerRenderer				visualizer;
	
	/**
	 * The value function renderer
	 */
	protected ValueFunctionRenderLayer			vfLayer;
	
	/**
	 * The policy renderer
	 */
	protected PolicyRenderLayer					pLayer;
	
	/**
	 * Painter used to visualize the value function
	 */
	protected StateValuePainter					svp;
	
	/**
	 * Painter used to visualize the policy
	 */
	protected StatePolicyPainter				spp = null;
	
	
	protected List <State>						statesToVisualize;
	
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
	 * A method for creating common 2D arrow glyped value function and policy visualization. The value of states
	 * will be represented by colored cells from red (lowest value) to blue (highest value). North-south-east-west
	 * actions will be rendered with arrows using {@link burlap.behavior.singleagent.auxiliary.valuefunctionvis.common.ArrowActionGlyph}
	 * objects. The GUI will not be launched by default; call the {@link #initGUI()} on the returned object to start it.
	 * @param states the states whose value should be rendered.
	 * @param valueFunction the valueFunction that can return the state values.
	 * @param p the policy to render
	 * @param classWithPositionAtts the class which contains the x-y position information of the state
	 * @param xAttName the name of the x attribute
	 * @param yAttName the name of the y attribute
	 * @param northActionName the name of the north action
	 * @param southActionName the name of the south action
	 * @param eastActionName the name of the east action
	 * @param westActionName the name of the west action
	 * @return a {@link burlap.behavior.singleagent.auxiliary.valuefunctionvis.ValueFunctionVisualizerGUI}
	 */
	public static ValueFunctionVisualizerGUI createGridWorldBasedValueFunctionVisualizerGUI(List <State> states, ValueFunction valueFunction, Policy p,
																 String classWithPositionAtts, String xAttName, String yAttName,
																 String northActionName,
																 String southActionName,
																 String eastActionName,
																 String westActionName){


		StateValuePainter2D svp = new StateValuePainter2D();
		svp.setXYAttByObjectClass(classWithPositionAtts, xAttName,
				classWithPositionAtts, yAttName);

		PolicyGlyphPainter2D spp = ArrowActionGlyph.getNSEWPolicyGlyphPainter(classWithPositionAtts, xAttName, yAttName,
				northActionName, southActionName, eastActionName, westActionName);

		ValueFunctionVisualizerGUI gui = new ValueFunctionVisualizerGUI(states, svp, valueFunction);
		gui.setSpp(spp);
		gui.setPolicy(p);
		gui.setBgColor(Color.GRAY);


		return gui;

	}



	
	/**
	 * Initializes the visualizer GUI.
	 * @param states the states whose value should be rendered.
	 * @param svp the value function state visualizer to use.
	 * @param valueFunction the valueFunction that can return the state values.
	 */
	public ValueFunctionVisualizerGUI(List <State> states, StateValuePainter svp, ValueFunction valueFunction){
		this.statesToVisualize = states;
		this.svp = svp;
		this.visualizer = new MultiLayerRenderer();
		this.vfLayer = new ValueFunctionRenderLayer(statesToVisualize, svp, valueFunction);
		this.pLayer = new PolicyRenderLayer(states, null, null);
		
		this.visualizer.addRenderLayer(vfLayer);
		this.visualizer.addRenderLayer(pLayer);
		
	}


	/**
	 * Returns the {@link burlap.oomdp.visualizer.MultiLayerRenderer} used in this GUI.
	 * @return the {@link burlap.oomdp.visualizer.MultiLayerRenderer} used in this GUI.
	 */
	public MultiLayerRenderer getMultiLayerRenderer(){
		return this.visualizer;
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
		this.vfLayer.setSvp(svp);
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
			this.pLayer.setSpp(spp);
		}
	}

	

	
	
	/**
	 * Sets the canvas background color
	 * @param col the canvas background color
	 */
	public void setBgColor(Color col){
		this.visualizer.setBGColor(col);
	}
	
	
	/**
	 * Sets the policy to render
	 * @param p the policy to render
	 */
	public void setPolicy(Policy p){
		this.pLayer.setPolicy(p);
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
	    		this.pLayer.setSpp(spp);
	    	}
	    	else{
	    		this.pLayer.setSpp(null);
	    	}
	    	this.visualizer.repaint();
	    }
	    
	}
	
	
}
