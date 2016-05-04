package burlap.oomdp.visualizer;

import burlap.oomdp.core.state.NullState;
import burlap.oomdp.core.state.State;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


/**
 * This class provides 2D visualization of states by being provided a set of state painters to iteratively call to paint
 * ono the canvas.
 * @author James MacGlashan
 *
 */
public class StateRenderLayer implements RenderLayer{

	/**
	 * the current state to be painted next
	 */
	protected State									curState;
	
	/**
	 * list of static painters that pain static non-object defined properties of the domain
	 */
	protected List <StatePainter> statePainters;


	
	public StateRenderLayer(){
		curState = NullState.instance;
		
		statePainters = new ArrayList <StatePainter>();
	}
	
	/**
	 * Adds a static painter for the domain.
	 * @param sp the static painter to add.
	 */
	public void addStatePainter(StatePainter sp){
		statePainters.add(sp);
	}
	

	
	
	/**
	 * Updates the state that needs to be painted
	 * @param s the state to paint
	 */
	public void updateState(State s){
		curState = s;
	}


	public State getCurState() {
		return curState;
	}

	public List<StatePainter> getStatePainters() {
		return statePainters;
	}


	@Override
	public void render(Graphics2D g2, float width, float height) {
		
		if(this.curState == null){
			return; //don't render anything if there is no state to render
		}
		
		//draw with each of the state painters
		for(StatePainter sp : statePainters){
			sp.paint(g2, curState, width, height);
		}
		
	}

	
	
	
}
