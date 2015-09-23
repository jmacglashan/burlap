package burlap.oomdp.stochasticgames.Callables;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.states.State;
import burlap.oomdp.stochasticgames.SGAgent;
import burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction;
import burlap.parallel.Parallel.ForEachCallable;

public class GetActionCallable extends ForEachCallable<List<SGAgent>, List<GroundedSGAgentAction>> {
	
	private final State abstractedCurrent;
	public GetActionCallable(State abstractedCurrent) {
		this.abstractedCurrent = abstractedCurrent;
	}
	
	@Override
	public List<GroundedSGAgentAction> perform(List<SGAgent> current) {
		List<GroundedSGAgentAction> actions = new ArrayList<GroundedSGAgentAction>();
		for (SGAgent agent : current) {
			actions.add(agent.getAction(abstractedCurrent));
		}
		return actions;
	}

	@Override
	public ForEachCallable<List<SGAgent>, List<GroundedSGAgentAction>> copy() {
		return new GetActionCallable(abstractedCurrent);
	}
}
