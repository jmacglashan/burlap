package burlap.mdp.singleagent.action;

import burlap.mdp.core.Action;
import burlap.mdp.core.SimpleAction;
import burlap.mdp.core.state.State;

import java.util.Arrays;
import java.util.List;

/**
 * An {@link ActionType} implementation for unparameterized actions that have no preconditions (can be executed anywhere).
 * Will generate a single {@link SimpleAction} whose action name is the same as the type for name for this {@link ActionType}.
 * @author James MacGlashan.
 */
public class UniversalActionType implements ActionType {

	/**
	 * The type name of this {@link ActionType}
	 */
	public String typeName;

	/**
	 * Initializes
	 * @param typeName The type name for this {@link ActionType}
	 */
	public UniversalActionType(String typeName) {
		this.typeName = typeName;
	}

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
