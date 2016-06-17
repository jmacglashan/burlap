package burlap.mdp.core.action;

import burlap.mdp.core.state.State;

import java.util.Arrays;
import java.util.List;

/**
 * An {@link ActionType} implementation for unparameterized actions (or at least a single action
 * whose parameters are full specified at construction time of this {@link ActionType}) that have no preconditions (can be executed anywhere).
 * The {@link #UniversalActionType(String)} constructor will cause this type to return {@link SimpleAction} instances whose name
 * match the provided type name for this object. The other constructors allow you to have this {@link ActionType} return a different
 * instance of {@link Action}.
 * Will generate a single {@link SimpleAction} whose action name is the same as the type for name for this {@link ActionType}.
 * @author James MacGlashan.
 */
public class UniversalActionType implements ActionType {

	/**
	 * The type name of this {@link ActionType}
	 */
	public String typeName;

	/**
	 * The {@link Action} object that will be returned.
	 */
	public Action action;

	/**
	 * The list single element list of the {@link Action} to be returned by the {@link #allApplicableActions(State)}
	 * method.
	 */
	protected List<Action> allActions;

	/**
	 * Initializes with the type name and sets to return a {@link SimpleAction} whose action name is the same as the
	 * type name.
	 * @param typeName The type name for this {@link ActionType}
	 */
	public UniversalActionType(String typeName) {
		this(new SimpleAction(typeName));
	}

	/**
	 * Initializes to return the given action. This object's type name will be the same as {@link Action#actionName()}
	 * for the input action
	 * @param action the input action to return
	 */
	public UniversalActionType(Action action) {
		this(action.actionName(), action);
	}

	/**
	 * Initializes.
	 * @param typeName the type name of this action type
	 * @param action the action to return
	 */
	public UniversalActionType(String typeName, Action action) {
		this.typeName = typeName;
		this.action = action;
		this.allActions = Arrays.asList(this.action);
	}

	@Override
	public String typeName() {
		return typeName;
	}

	@Override
	public Action associatedAction(String strRep) {
		return action;
	}

	@Override
	public List<Action> allApplicableActions(State s) {
		return allActions;
	}

}
