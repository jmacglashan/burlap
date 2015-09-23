package burlap.behavior.stochasticgame.saconversion;

import java.util.List;

import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;

public class RTDPGreedyQPolicy extends GreedyQPolicy {
	
	public RTDPGreedyQPolicy(QComputablePlanner planner) {
		super(planner);
	}

	@Override
	public AbstractGroundedAction getAction(State s) {
		((OOMDPPlanner)this.qplanner).planFromState(s);
		return super.getAction(s);

	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		
		((OOMDPPlanner)this.qplanner).planFromState(s);
		return super. getActionDistributionForState(s);

	}

}
