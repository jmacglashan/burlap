package burlap.oomdp.visualizer;


import java.awt.Color;

import burlap.oomdp.core.states.State;

/**
 * This class extends the {@link MultiLayerRenderer} class to provide a base instance of a {@link StateRenderLayer} in its render list
 * and provides methods to directly access and interface with the {@link StateRenderLayer} instance. 
 * <p/>
 * The {@link StateRenderLayer} instance provides 2D visualization of states by being provided a set of classes that can paint
 * ObjectInstnaces to the canvas as well as classes that can paint general domain information. Painters
 * for object classes as well as specific object instances can be provided. If there is a painter
 * for an object class and a painter for a specific object instance of that same class, then the specific object instance
 * painter will be used to pain that object instead of the painter for that instnace's OO-MDP class.
 * @author James MacGlashan
 *
 */
public class Visualizer extends MultiLayerRenderer{


	private static final long serialVersionUID = 1L;
	
	
	/**
	 * The {@link StateRenderLayer} instance for visualizing OO-MDP states.
	 */
	protected StateRenderLayer			srender;
	
	
	
	public Visualizer(){
		super();
		srender = new StateRenderLayer();
		this.renderLayers.add(srender);
	}
	
	public Visualizer(StateRenderLayer srender){
		super();
		this.srender = srender;
		this.renderLayers.add(this.srender);
	}
	
	public void setSetRenderLayer(StateRenderLayer srender){
		this.renderLayers.remove(this.srender);
		this.renderLayers.add(srender);
		this.srender = srender;
	}
	
	/**
	 * Sets the background color of the canvas
	 * @param c the background color of the canvas
	 */
	public void setBGColor(Color c){
		this.bgColor = c;
	}
	
	/**
	 * Adds a static painter for the domain.
	 * @param sp the static painter to add.
	 */
	public void addStaticPainter(StaticPainter sp){
		this.srender.addStaticPainter(sp);
	}
	
	
	/**
	 * Adds a class that will paint objects that belong to a given OO-MDPclass.
	 * @param className the name of the class that the provided painter can paint
	 * @param op the painter
	 */
	public void addObjectClassPainter(String className, ObjectPainter op){
		this.srender.addObjectClassPainter(className, op);
	}
	
	
	/**
	 * Adds a painter that will be used to paint a specific object in states
	 * @param objectName the name of the object this painter is used to paint
	 * @param op the painter
	 */
	public void addSpecificObjectPainter(String objectName, ObjectPainter op){
		this.srender.addSpecificObjectPainter(objectName, op);
	}
	
	/**
	 * Returns the {@link StateRenderLayer} instance for visualizing OO-MDP states.
	 * @return the {@link StateRenderLayer} instance for visualizing OO-MDP states.
	 */
	public StateRenderLayer getStateRenderLayer(){
		return this.srender;
	}
	
	
	/**
	 * Updates the state that needs to be painted and repaints.
	 * @param s the state to paint
	 */
	public void updateState(State s){
		this.srender.updateState(s);
		repaint();
	}
	
	
	
	
	
	
	
	
	
}
