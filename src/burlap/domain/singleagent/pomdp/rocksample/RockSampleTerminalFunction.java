package burlap.domain.singleagent.pomdp.rocksample;

import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;

public class RockSampleTerminalFunction implements TerminalFunction{
	@Override
	public boolean isTerminal(State s) {
		ObjectInstance rover = s.getObject(Names.OBJ_AGENT);
		if (rover.getBooleanValue(Names.ATTR_COMPLETE)){return true;}
		return false;
	}
}
