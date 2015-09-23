package burlap.behavior.stochasticgames.saconversion;

import java.util.List;

import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.singleagent.MDPSolver;
import burlap.behavior.singleagent.MDPSolverInterface;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.valuefunction.QFunction;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.states.State;

public class RTDPGreedyQPolicy extends GreedyQPolicy {
	
	public RTDPGreedyQPolicy(QFunction planner) {
		super(planner);
	}

	@Override
	public AbstractGroundedAction getAction(State s) {
		((Planner)this.qplanner).planFromState(s);
		return super.getAction(s);

	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		
		((Planner)this.qplanner).planFromState(s);
		return super. getActionDistributionForState(s);

	}

}
