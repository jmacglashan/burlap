package burlap.mdp.singleagent;

import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;

import java.util.Arrays;
import java.util.List;

/**
 * @author James MacGlashan.
 */
public class UniversalActionType implements ActionType {

	public String typeName;

	@Override
	public String typeName() {
		return typeName;
	}

	@Override
	public Action associatedAction(String strRep) {
		return new UniversalAction(typeName);
	}

	@Override
	public List<Action> allApplicableActions(State s) {
		return Arrays.<Action>asList(new UniversalAction(typeName));
	}

	public static class UniversalAction implements Action{

		String name = "undefined_action";

		public UniversalAction() {
		}

		public UniversalAction(String name) {
			this.name = name;
		}

		@Override
		public String actionName() {
			return name;
		}

		@Override
		public Action copy() {
			return new UniversalAction(name);
		}

		@Override
		public boolean applicableInState(State s) {
			return true;
		}

		@Override
		public int hashCode() {
			return name.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if(this == o) return true;
			if(o == null || getClass() != o.getClass()) return false;

			UniversalAction that = (UniversalAction) o;

			return name != null ? name.equals(that.name) : that.name == null;

		}

		@Override
		public String toString() {
			return name.toString();
		}
	}
}
