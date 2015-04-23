package burlap.oomdp.singleagent.explorer;

import burlap.oomdp.auxiliary.StateGenerator;
import burlap.oomdp.auxiliary.common.ConstantStateGenerator;
import burlap.oomdp.core.states.State;


/**
 * A special non-domain action that causes a visual explorer to rest the state to a specified base state or to a state drawn from a state generator.
 * @author James MacGlashan
 *
 */
public class StateResetSpecialAction implements SpecialExplorerAction {

	StateGenerator stateGenerator;
	
	/**
	 * Initializes which base state to reset to
	 * @param s the state to reset to when this action is executed
	 */
	public StateResetSpecialAction(State s){
		this.stateGenerator = new ConstantStateGenerator(s);
	}
	
	/**
	 * Initializes with a state generator to draw from on reset
	 * @param stateGenerator the state generate to draw from.
	 */
	public StateResetSpecialAction(StateGenerator stateGenerator){
		this.stateGenerator = stateGenerator;
	}
	
	/**
	 * Sets the base state to reset to
	 * @param s the state to reset to when this action is executed
	 */
	public void setBase(State s){
		this.stateGenerator = new ConstantStateGenerator(s);
	}
	
	/**
	 * Sets the state generator to draw from on reset
	 * @param stateGenerator the state generator to draw from on reset
	 */
	public void setBaseStateGenerator(StateGenerator stateGenerator){
		this.stateGenerator = stateGenerator;
	}
	
	@Override
	public State applySpecialAction(State curState) {
		return this.stateGenerator.generateState();
	}

}
