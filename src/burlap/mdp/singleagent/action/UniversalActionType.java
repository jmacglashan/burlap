package burlap.mdp.singleagent.action;

import burlap.mdp.core.Action;
import burlap.mdp.core.SimpleAction;
import burlap.mdp.core.state.State;

import java.util.Arrays;
import java.util.List;

/**
 * @author James MacGlashan.
 */
public class UniversalActionType implements ActionType {

	public UniversalActionType(String typeName) {
		this.typeName = typeName;
	}

	public String typeName;

	@Override
	public String typeName() {
		return typeName;
	}

	@Override
	public Action associatedAction(String strRep) {
		return new SimpleAction(typeName);
	}

	@Override
	public List<Action> allApplicableActions(State s) {
		return Arrays.<Action>asList(new SimpleAction(typeName));
	}

}
