package burlap.oomdp.stochasticgames.agentactions;

import burlap.oomdp.core.state.State;
import burlap.oomdp.stochasticgames.SGDomain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * This {@link burlap.oomdp.stochasticgames.agentactions.SGAgentAction} definition defines a parameter-less agent action
 * that can be
 * executed in every state. This is a useful action definition for symmetric games.
 * @author James MacGlashan
 *
 */
public class SimpleSGAgentAction extends SGAgentAction {

	/**
	 * Initializes this single action to be for the given domain and with the given name. This action
	 * is automatically added to the given domain
	 * @param d the domain to which this action belongs
	 * @param name the name of this action
	 */
	public SimpleSGAgentAction(SGDomain d, String name) {
		super(d, name);
	}
	



	@Override
	public boolean applicableInState(State s, GroundedSGAgentAction gsa) {
		return true;
	}

	@Override
	public boolean isParameterized() {
		return false;
	}

	@Override
	public GroundedSGAgentAction getAssociatedGroundedAction(String actingAgent) {
		return new SimpleGroundedSGAgentAction(actingAgent, this);
	}

	@Override
	public List<GroundedSGAgentAction> getAllApplicableGroundedActions(State s, String actingAgent) {
		GroundedSGAgentAction gaa = this.getAssociatedGroundedAction(actingAgent);
		return this.applicableInState(s, gaa) ? Arrays.asList(gaa) : new ArrayList<GroundedSGAgentAction>(0);
	}
}
