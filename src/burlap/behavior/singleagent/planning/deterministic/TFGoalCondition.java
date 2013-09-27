package burlap.behavior.singleagent.planning.deterministic;

import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;

public class TFGoalCondition implements StateConditionTest {

	protected TerminalFunction tf;
	
	public TFGoalCondition(TerminalFunction tf){
		this.tf = tf;
	}
	
	@Override
	public boolean satisfies(State s) {
		return tf.isTerminal(s);
	}

}
