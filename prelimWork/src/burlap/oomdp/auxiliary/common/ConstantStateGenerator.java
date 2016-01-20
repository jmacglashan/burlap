package burlap.oomdp.auxiliary.common;

import burlap.oomdp.auxiliary.StateGenerator;
import burlap.oomdp.core.State;

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
