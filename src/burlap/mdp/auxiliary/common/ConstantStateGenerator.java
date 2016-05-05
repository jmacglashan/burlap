package burlap.mdp.auxiliary.common;

import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.core.state.State;

/**
 * This class takes a source state as input as returns copies of it for every call of generateState().
 * @author James MacGlashan
 *
 */
public class ConstantStateGenerator implements StateGenerator {

	protected State src;
	
	/**
	 * This class takes a source state as input as returns copies of it for every call of generateState().
	 * @param src the source state of which to return copies
	 */
	public ConstantStateGenerator(State src){
		this.src = src;
	}
	
	@Override
	public State generateState() {
		return src.copy();
	}

}
