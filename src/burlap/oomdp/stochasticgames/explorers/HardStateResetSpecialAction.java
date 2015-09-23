package burlap.oomdp.stochasticgames.explorers;

import burlap.oomdp.auxiliary.StateGenerator;
import burlap.oomdp.auxiliary.common.ConstantStateGenerator;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.explorer.SpecialExplorerAction;

/**
 * @author James MacGlashan.
 */
public class HardStateResetSpecialAction implements SpecialExplorerAction {

	StateGenerator stateGenerator;

	/**
	 * Initializes which base state to reset to
	 * @param s the state to reset to when this action is executed
	 */
	public HardStateResetSpecialAction(State s){
		this.stateGenerator = new ConstantStateGenerator(s);
	}

	/**
	 * Initializes with a state generator to draw from on reset
	 * @param stateGenerator the state generate to draw from.
	 */
	public HardStateResetSpecialAction(StateGenerator stateGenerator){
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
	public void setBaseStateGenerator(StateGenerator stateGenerator) {
		this.stateGenerator = stateGenerator;
	}

	@Override
	public State applySpecialAction(State curState) {
		return this.stateGenerator.generateState();
	}
}
