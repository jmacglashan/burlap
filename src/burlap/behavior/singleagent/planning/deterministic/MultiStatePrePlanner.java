package burlap.behavior.singleagent.planning.deterministic;

import java.util.Collection;

import burlap.behavior.singleagent.planning.Planner;
import burlap.oomdp.auxiliary.stateconditiontest.StateConditionTestIterable;
import burlap.oomdp.core.states.State;


/**
 * This is a helper class that is used to run a valueFunction from multiple initial states to ensure
 * that an adequate plan/policy exists for each them. It makes uses of an iterable state
 * condition test to define the states from which planning should performed or a collection
 * of state objects.
 * @author James MacGlashan
 *
 */
public class MultiStatePrePlanner {

    private MultiStatePrePlanner() {
        // do nothing
    }
    
	/**
	 * Runs a planning algorithm from multiple initial states to ensure that an adequate plan/policy exist for of the states.
	 * @param planner the valueFunction to be used.
	 * @param initialStates a {@link burlap.oomdp.auxiliary.stateconditiontest.StateConditionTestIterable} object that will iterate over the initial states from which to plan.
	 */
	public static void runPlannerForAllInitStates(Planner planner, StateConditionTestIterable initialStates){
		for(State s : initialStates){
			planner.planFromState(s);
		}
	}
	
	
	/**
	 * Runs a planning algorithm from multiple initial states to ensure that an adequate plan/policy exist for of the states.
	 * @param planner the valueFunction to be used.
	 * @param initialStates a collection of states from which to plan.
	 */
	public static void runPlannerForAllInitStates(Planner planner, Collection <State> initialStates){
		for(State s : initialStates){
			planner.planFromState(s);
		}
	}
	
}
