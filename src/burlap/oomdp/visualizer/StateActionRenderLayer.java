package burlap.oomdp.visualizer;

import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.states.State;

import java.awt.*;

/**
 * A class for rendering state-action events. This class will maintain the current {@link burlap.oomdp.core.states.State}
 * and {@link burlap.oomdp.core.AbstractGroundedAction} to render. Subclasses need to implement the
 * {@link #renderStateAction(java.awt.Graphics2D, burlap.oomdp.core.states.State, burlap.oomdp.core.AbstractGroundedAction, float, float)}
 * method.
 * @author James MacGlashan.
 */
public abstract class StateActionRenderLayer implements RenderLayer {

	/**
	 * The current {@link burlap.oomdp.core.states.State} to render
	 */
	protected State renderState = null;

	/**
	 * The current {@link burlap.oomdp.core.AbstractGroundedAction} to render
	 */
	protected AbstractGroundedAction renderAction = null;


	/**
	 * Returns the {@link burlap.oomdp.core.states.State} that is/will be rendered
	 * @return a {@link burlap.oomdp.core.states.State}
	 */
	public State getRenderState() {
		return renderState;
	}


	/**
	 * Returns the {@link burlap.oomdp.core.AbstractGroundedAction} that is/will be rendered
	 * @return a {@link burlap.oomdp.core.AbstractGroundedAction}
	 */
	public AbstractGroundedAction getRenderAction() {
		return renderAction;
	}


	/**
	 * Updates the {@link burlap.oomdp.core.states.State} and {@link burlap.oomdp.core.AbstractGroundedAction} that will
	 * be rendered the next time this class draws
	 * @param s a {@link burlap.oomdp.core.states.State} to render
	 * @param a a {@link burlap.oomdp.core.AbstractGroundedAction} to render
	 */
	public void updateRenderedStateAction(State s, AbstractGroundedAction a){
		this.renderState = s;
		this.renderAction = a;
	}


	/**
	 * Sets the {@link burlap.oomdp.core.states.State} and {@link burlap.oomdp.core.AbstractGroundedAction} to
	 * render to null, which will prevent calls to the method {@link #renderStateAction(java.awt.Graphics2D, burlap.oomdp.core.states.State, burlap.oomdp.core.AbstractGroundedAction, float, float)}
	 * to be made.
	 */
	public void clearRenderedStateAction(){
		this.renderState = null;
		this.renderAction = null;
	}

	@Override
	public void render(Graphics2D g2, float width, float height) {
		if(this.renderAction != null){
			this.renderStateAction(g2, this.renderState, this.renderAction, width, height);
		}
	}


	/**
	 * Method to be implemented by subclasses that will render the input state-action to the given graphics context.
	 * @param g2 the {@link java.awt.Graphics2D} to which to render
	 * @param s the {@link burlap.oomdp.core.states.State} to render
	 * @param a the {@link burlap.oomdp.core.AbstractGroundedAction} to render
	 * @param width the width of the graphics context
	 * @param height the height of hte graphics context
	 */
	public abstract void renderStateAction(Graphics2D g2, State s, AbstractGroundedAction a, float width, float height);

}
