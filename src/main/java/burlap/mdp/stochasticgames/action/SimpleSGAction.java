package burlap.mdp.stochasticgames.action;

import burlap.mdp.core.Action;

/**
 * @author James MacGlashan.
 */
public class SimpleSGAction implements SGAgentAction {

	public String name;
	public String actingAgent;

	public SimpleSGAction() {
	}

	public SimpleSGAction(String name, String actingAgent) {
		this.name = name;
		this.actingAgent = actingAgent;
	}

	@Override
	public String actingAgent() {
		return actingAgent;
	}

	@Override
	public void setActingAgent(String actingAgent) {
		this.actingAgent = actingAgent;
	}

	@Override
	public String actionName() {
		return name;
	}

	@Override
	public Action copy() {
		return new SimpleSGAction(name, actingAgent);
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;

		SimpleSGAction that = (SimpleSGAction) o;

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
