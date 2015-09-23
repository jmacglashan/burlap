package burlap.domain.singleagent.graphdefined;

import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

/**
 * An abstract class for more easily defining {@link burlap.oomdp.singleagent.RewardFunction}s for {@link burlap.domain.singleagent.graphdefined.GraphDefinedDomain}
 * {@link burlap.oomdp.core.Domain}s. This class implements the standard {@link #reward(burlap.oomdp.core.State, burlap.oomdp.singleagent.GroundedAction, burlap.oomdp.core.State)}
 * method by converting the {@link burlap.oomdp.core.State} objects to their graph node integer representation and the {@link burlap.oomdp.singleagent.GroundedAction} to its
 * integer representation and then returning the value of {@link #reward(int, int, int)}, which is an abstract method
 * that the client must implement.
 * @author James MacGlashan.
 */
public abstract class GraphRF implements RewardFunction{

	@Override
	public double reward(State s, GroundedAction a, State sprime) {
		int actionId = Integer.parseInt(a.toString().replaceAll(GraphDefinedDomain.BASEACTIONNAME, ""));
		return this.reward(GraphDefinedDomain.getNodeId(s), actionId, GraphDefinedDomain.getNodeId(sprime));
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
