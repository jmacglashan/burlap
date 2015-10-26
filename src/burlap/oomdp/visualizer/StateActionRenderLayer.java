package burlap.oomdp.visualizer;

import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;

import java.awt.*;

/**
 * @author James MacGlashan.
 */
public abstract class StateActionRenderLayer implements RenderLayer {

	protected State renderState = null;
	protected GroundedAction renderAction = null;

	public State getRenderState() {
		return renderState;
	}

	public GroundedAction getRenderAction() {
		return renderAction;
	}

	public void updateRenderedStateAction(State s, GroundedAction a){
		this.renderState = s;
		this.renderAction = a;
	}

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

	public abstract void renderStateAction(Graphics2D g2, State s, GroundedAction a, float width, float height);

}
