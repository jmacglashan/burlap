package burlap.mdp.stochasticgames.agentactions;

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
		return new SimpleSGAction(name, actingAgent);
	}

	@Override
	public List<SGAgentAction> allApplicableActions(String actingAgent, State s) {
		return Arrays.<SGAgentAction>asList(new SimpleSGAction(this.name, actingAgent));
	}


}
