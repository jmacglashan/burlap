package burlap.visualizer;

import burlap.mdp.core.Action;
import burlap.mdp.core.state.NullState;
import burlap.mdp.core.state.State;

import java.awt.*;

/**
 * A class for rendering state-action events. This class will maintain the current {@link State}
 * and {@link Action} to render. Subclasses need to implement the
 * {@link #renderStateAction(java.awt.Graphics2D, State, Action, float, float)}
 * method.
 * @author James MacGlashan.
 */
public abstract class StateActionRenderLayer implements RenderLayer {

	/**
	 * The current {@link State} to render
	 */
	protected State renderState = null;

	/**
	 * The current {@link Action} to render
	 */
	protected Action renderAction = null;


	/**
	 * Returns the {@link State} that is/will be rendered
	 * @return a {@link State}
	 */
	public State getRenderState() {
		return renderState;
	}


	/**
	 * Returns the {@link Action} that is/will be rendered
	 * @return a {@link Action}
	 */
	public Action getRenderAction() {
		return renderAction;
	}


	/**
	 * Updates the {@link State} and {@link Action} that will
	 * be rendered the next time this class draws
	 * @param s a {@link State} to render
	 * @param a a {@link Action} to render
	 */
	public void updateRenderedStateAction(State s, Action a){
		this.renderState = s;
		this.renderAction = a;
	}


	/**
	 * Sets the {@link State} and {@link Action} to
	 * render to null, which will prevent calls to the method {@link #renderStateAction(java.awt.Graphics2D, State, Action, float, float)}
	 * to be made.
	 */
	public void clearRenderedStateAction(){
		this.renderState = null;
		this.renderAction = null;
	}

	@Override
	public void render(Graphics2D g2, float width, float height) {
		if(this.renderAction != null && this.renderState != null && !(this.renderState instanceof NullState)){
			this.renderStateAction(g2, this.renderState, this.renderAction, width, height);
		}
	}


	/**
	 * Method to be implemented by subclasses that will render the input state-action to the given graphics context.
	 * @param g2 the {@link java.awt.Graphics2D} to which to render
	 * @param s the {@link State} to render
	 * @param a the {@link Action} to render
	 * @param width the width of the graphics context
	 * @param height the height of hte graphics context
	 */
	public abstract void renderStateAction(Graphics2D g2, State s, Action a, float width, float height);

}
