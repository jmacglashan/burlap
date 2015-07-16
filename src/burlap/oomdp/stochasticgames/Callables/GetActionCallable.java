package burlap.oomdp.stochasticgames.Callables;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.Agent;
import burlap.oomdp.stochasticgames.GroundedSingleAction;
import burlap.parallel.Parallel.ForEachCallable;

public class GetActionCallable extends ForEachCallable<List<Agent>, List<GroundedSingleAction>> {
	
	private final State abstractedCurrent;
	public GetActionCallable(State abstractedCurrent) {
		this.abstractedCurrent = abstractedCurrent;
	}
	
	@Override
	public List<GroundedSingleAction> perform(List<Agent> current) {
		List<GroundedSingleAction> actions = new ArrayList<GroundedSingleAction>();
		for (Agent agent : current) {
			actions.add(agent.getAction(abstractedCurrent));
		}
		return actions;
	}

	@Override
	public ForEachCallable<List<Agent>, List<GroundedSingleAction>> copy() {
		return new GetActionCallable(abstractedCurrent);
	}
}
