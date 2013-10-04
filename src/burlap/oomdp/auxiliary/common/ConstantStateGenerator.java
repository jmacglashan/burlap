package burlap.oomdp.auxiliary.common;

import burlap.oomdp.auxiliary.StateGenerator;
import burlap.oomdp.core.State;

public class ConstantStateGenerator implements StateGenerator {

	protected State src;
	
	public ConstantStateGenerator(State src){
		this.src = src;
	}
	
	@Override
	public State generateState() {
		return src.copy();
	}

}
