package burlap.mdp.stochasticgames.agentactions;

import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;

import java.util.Arrays;
import java.util.List;

/**
 * @author James MacGlashan.
 */
public class UniversalSGActionType implements SGAgentActionType {

	String name;

	public UniversalSGActionType(String name) {
		this.name = name;
	}

	@Override
	public String typeName() {
		return name;
	}

	@Override
	public SGAgentAction associatedAction(String actingAgent, String strRep) {
		return new UniversalSGAgentAction(name, actingAgent);
	}

	@Override
	public List<SGAgentAction> allApplicableActions(String actingAgent, State s) {
		return Arrays.<SGAgentAction>asList(new UniversalSGAgentAction(this.name, actingAgent));
	}


	public static class UniversalSGAgentAction implements SGAgentAction{

		public String name;
		public String actingAgent;

		public UniversalSGAgentAction() {
		}

		public UniversalSGAgentAction(String name, String actingAgent) {
			this.name = name;
			this.actingAgent = actingAgent;
		}

		@Override
		public String actingAgent() {
			return actingAgent;
		}

		@Override
		public String actionName() {
			return name;
		}

		@Override
		public Action copy() {
			return null;
		}

		@Override
		public boolean applicableInState(State s) {
			return true;
		}

		@Override
		public boolean equals(Object o) {
			if(this == o) return true;
			if(o == null || getClass() != o.getClass()) return false;

			UniversalSGAgentAction that = (UniversalSGAgentAction) o;

			if(name != null ? !name.equals(that.name) : that.name != null) return false;
			return actingAgent != null ? actingAgent.equals(that.actingAgent) : that.actingAgent == null;

		}

		@Override
		public int hashCode() {
			int result = name != null ? name.hashCode() : 0;
			result = 31 * result + (actingAgent != null ? actingAgent.hashCode() : 0);
			return result;
		}

		@Override
		public String toString() {
			return actingAgent + ":" + name;
		}
	}
}
