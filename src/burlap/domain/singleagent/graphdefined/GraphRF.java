package burlap.domain.singleagent.graphdefined;

import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;

/**
 * An abstract class for more easily defining {@link RewardFunction}s for {@link burlap.domain.singleagent.graphdefined.GraphDefinedDomain}
 * {@link burlap.mdp.core.Domain}s. This class implements the standard {@link #reward(State, Action, State)}
 * method by converting the {@link State} objects to their graph node integer representation and the {@link Action} to its
 * integer representation and then returning the value of {@link #reward(int, int, int)}, which is an abstract method
 * that the client must implement.
 * @author James MacGlashan.
 */
public abstract class GraphRF implements RewardFunction{

	@Override
	public double reward(State s, Action a, State sprime) {
		int actionId = Integer.parseInt(a.toString().replaceAll(GraphDefinedDomain.BASE_ACTION_NAME, ""));
		return this.reward(((GraphStateNode)s).id, actionId, ((GraphStateNode)sprime).id);
	}

	/**
	 * Returns the reward for taking action a in state node s and transition to state node sprime.
	 * @param s the previous state node id
	 * @param a the action id
	 * @param sprime the next state node id
	 * @return the received reward for the transition in the graph
	 */
	public abstract double reward(int s, int a, int sprime);
}
