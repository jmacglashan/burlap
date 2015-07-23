package burlap.oomdp.singleagent.explorer;

import burlap.oomdp.auxiliary.StateGenerator;
import burlap.oomdp.auxiliary.common.ConstantStateGenerator;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.environment.Environment;


/**
 * A special non-domain action that causes a {@link burlap.oomdp.singleagent.explorer.VisualExplorer}'s environment to be reset with the {@link burlap.oomdp.singleagent.environment.Environment#resetEnvironment()}
 * @author James MacGlashan
 *
 */
public class StateResetSpecialAction implements SpecialExplorerAction {

	Environment env;
	
	/**
	 * Initializes.
	 * @param env the {@link burlap.oomdp.singleagent.environment.Environment} which will be reset by the {@link #applySpecialAction(burlap.oomdp.core.State)} method.
	 */
	public StateResetSpecialAction(Environment env){
		this.env = env;
	}

	
	@Override
	public State applySpecialAction(State curState) {
		this.env.resetEnvironment();
		return this.env.getCurState();
	}

}
