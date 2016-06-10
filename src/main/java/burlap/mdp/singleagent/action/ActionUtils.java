package burlap.mdp.singleagent.action;

import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;

import java.util.ArrayList;
import java.util.List;

/**
 * A class holding static methods for common {@link Action} and {@link ActionType} operations.
 * @author James MacGlashan.
 */
public class ActionUtils {

	/**
	 * Returns all {@link Action}s that are applicable in the given {@link State} for all {@link ActionType} objects in the provided list. This method
	 * operates by calling the {@link ActionType#allApplicableActions(State)} method on each action and adding all the results
	 * to a list that is then returned.
	 * @param actionTypes The list of all actions for which grounded actions should be returned.
	 * @param s the state
	 * @return a {@link List} of all the {@link Action}s for all {@link ActionType} in the list that are applicable in the given {@link State}
	 */
	public static List<Action> allApplicableActionsForTypes(List<ActionType> actionTypes, State s){
		List<Action> res = new ArrayList<Action>();
		for(ActionType a : actionTypes){
			res.addAll(a.allApplicableActions(s));
		}
		return res;
	}
}
