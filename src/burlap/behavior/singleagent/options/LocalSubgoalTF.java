package burlap.behavior.singleagent.options;

import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;


/**
 * It is typical for options to be defined for following policies to subgoals and it is often useful
 * to use a planning or learning algorithm to define these policies, in which case a terminal
 * function for the option would need to be specified in order to learn or plan for its policy. This terminal function 
 * defines a set of states in which an option is applicable and the subgoal states of the option.
 * The subgoal state and applicable states are specified using {@link burlap.behavior.singleagent.planning.StateConditionTest}
 * objects. The agent will terminate in any subgoal state or any state that is not an applicable state.
 * @author James MacGlashan
 *
 */
public class LocalSubgoalTF implements TerminalFunction {

	
	/**
	 * Defines the set of states in which the option is applicable
	 */
	protected StateConditionTest		applicableStateTest;
	
	/**
	 * Defines he set of subgoal states for the option
	 */
	protected StateConditionTest		subgoalStateTest;
	
	
	
	/**
	 * Initializes with a set of subgoal states. The option is assumed to be applicable everywhere.
	 * @param subgoalStateTest the subgoal states.
	 */
	public LocalSubgoalTF(StateConditionTest subgoalStateTest) {
		this.applicableStateTest = null;
		this.subgoalStateTest = subgoalStateTest;
	}
	
	
	/**
	 * Initializes with a set of states in which the option is applicable and the options subgoal states.
	 * @param applicableStateTest the states in which the option is applicable.
	 * @param subgoalStateTest the subgoal states
	 */
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
