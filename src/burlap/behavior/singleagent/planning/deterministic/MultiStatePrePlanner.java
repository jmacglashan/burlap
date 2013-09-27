package burlap.behavior.singleagent.planning.deterministic;

import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.StateConditionTestIterable;
import burlap.oomdp.core.State;

public class MultiStatePrePlanner {

	
	public static void runPlannerForAllInitStates(OOMDPPlanner planner, StateConditionTestIterable initialStates){
		for(State s : initialStates){
			planner.planFromState(s);
		}
	}
	
}
