package burlap.oomdp.auxiliary.common;

import burlap.oomdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.oomdp.core.states.State;
import burlap.oomdp.core.TerminalFunction;


/**
 * Creates a terminal function that indicates terminal states are any states that satisfy a goal condition
 * where the goal condition is specified by a {@link burlap.oomdp.auxiliary.stateconditiontest.StateConditionTest} object.
 * No other states are set as terminal states.
 * @author James MacGlashan
 *
 */
public class GoalConditionTF implements TerminalFunction {

	/**
	 * The state condition test that is used to indicate terminal goal states
	 */
	StateConditionTest		goalCondition;
	
	public GoalConditionTF(StateConditionTest goalCondition) {
		this.goalCondition = goalCondition;
	}
	
	@Override
	public boolean isTerminal(State s) {
		return this.goalCondition.satisfies(s);
	}

}
