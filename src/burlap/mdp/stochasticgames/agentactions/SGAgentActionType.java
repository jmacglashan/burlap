package burlap.mdp.stochasticgames.agentactions;

import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.ActionType;

import java.util.List;

/**
 *
 *
 * @author James MacGlashan
 *
 */
public interface SGAgentActionType {

	String typeName();

	SGAgentAction associatedAction(String actingAgent, String strRep);

	/**
	 * Returns all possible actions of this type that can be applied in the provided {@link State}.
	 * @param s the {@link State} in which all applicable  actions of this {@link ActionType} object should be returned.
	 * @return a list of all applicable {@link Action}s of this {@link ActionType} object in in the given {@link State}
	 */
	List<SGAgentAction> allApplicableActions(String actingAgent, State s);

}
