package burlap.behavior.singleagent.pomdp.pomcp;

import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.behavior.singleagent.pomdp.POMDPPlanner;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.pomdp.BeliefState;

public abstract class MonteCarloPOMDPPlanner extends POMDPPlanner implements QComputablePlanner {
	public abstract GroundedAction getAction(State Observation, GroundedAction previousAction );
	
	public abstract BeliefState getBeliefState();
	
	public abstract GroundedAction getCurrentBestAction();

}
