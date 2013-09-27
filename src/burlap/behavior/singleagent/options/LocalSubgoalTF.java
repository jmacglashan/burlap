package burlap.behavior.singleagent.options;

import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;

public class LocalSubgoalTF implements TerminalFunction {

	
	protected StateConditionTest		applicableStateTest;
	protected StateConditionTest		subgoalStateTest;
	
	
	public LocalSubgoalTF(StateConditionTest subgoalStateTest) {
		this.applicableStateTest = null;
		this.subgoalStateTest = subgoalStateTest;
	}
	
	public LocalSubgoalTF(StateConditionTest applicableStateTest, StateConditionTest subgoalStateTest) {
		this.applicableStateTest = applicableStateTest;
		this.subgoalStateTest = subgoalStateTest;
	}

	@Override
	public boolean isTerminal(State s) {
		
		if(this.applicableStateTest != null){
			if(!this.applicableStateTest.satisfies(s)){
				return true; //terminate when reaching a state that is not an initiation state
			}
		}
		
		return this.subgoalStateTest.satisfies(s);

	}

}
