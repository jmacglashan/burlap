package burlap.mdp.stochasticgames.action;

import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.action.ActionType;

import java.util.List;

/**
 * A generator for {@link SGAgentAction} objects that is sensitive to any state-dependent preconditions. You
 * must specify a unique name for this action type, and implement methods for getting an {@link SGAgentAction}
 * for a given string representation of action any action parameters and for getting all applicable actions
 * for a given state (i.e., actions whose preconditions, if any, are satisfies). See the relevant method
 * java docs for more information.
 *
 * @author James MacGlashan
 *
 */
public interface SGAgentActionType {


	/**
	 * Returns the type name of this {@link SGAgentActionType}
	 * @return the type name of this {@link SGAgentActionType}
	 */
	String typeName();

	/**
	 * Returns a {@link SGAgentAction} where the parameters for the action are specified in the provided string
	 * representation (may be empty or null if the action is not parameterized).
	 * @param actingAgent the name of the acting agent for which the action will be generated
	 * @param strRep the string representation of any parameters for the action
	 * @return an {@link SGAgentAction}
	 */
	SGAgentAction associatedAction(String actingAgent, String strRep);

	/**
	 * Returns all possible actions of this type that can be applied in the provided {@link State}.
	 * @param actingAgent the name of the acting agent for which actions will be generated
	 * @param s the {@link State} in which all applicable  actions of this {@link ActionType} object should be returned.
	 * @return a list of all applicable {@link Action}s of this {@link ActionType} object in in the given {@link State}
	 */
	List<SGAgentAction> allApplicableActions(String actingAgent, State s);

}
