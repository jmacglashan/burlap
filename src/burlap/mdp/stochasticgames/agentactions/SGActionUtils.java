package burlap.mdp.stochasticgames.agentactions;

import burlap.mdp.core.state.State;

import java.util.ArrayList;
import java.util.List;

/**
 * @author James MacGlashan.
 */
public class SGActionUtils {

	public static List<SGAgentAction> allApplicableActionsForTypes(List<SGAgentActionType> actionTypes, String actingAgent, State s){
		List<SGAgentAction> actions = new ArrayList<SGAgentAction>();
		for(SGAgentActionType actionType : actionTypes){
			List<SGAgentAction> as = actionType.allApplicableActions(actingAgent, s);
			actions.addAll(as);
		}
		return actions;
	}

}
