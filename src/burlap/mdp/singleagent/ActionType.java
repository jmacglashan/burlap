package burlap.mdp.singleagent;

import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;

import java.util.List;


/**
 *
 *
 * @author James MacGlashan
 *
 */
public interface ActionType {


	String typeName();

	Action associatedAction(String strRep);

	/**
	 * Returns all possible actions of this type that can be applied in the provided {@link State}.
	 * @param s the {@link State} in which all applicable  actions of this {@link ActionType} object should be returned.
	 * @return a list of all applicable {@link Action}s of this {@link ActionType} object in in the given {@link State}
	 */
	List<Action> allApplicableActions(State s);
	

	
	
}
