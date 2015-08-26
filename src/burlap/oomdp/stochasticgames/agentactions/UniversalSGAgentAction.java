package burlap.oomdp.stochasticgames.agentactions;

import burlap.oomdp.core.states.State;
import burlap.oomdp.stochasticgames.SGDomain;
import burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction;
import burlap.oomdp.stochasticgames.agentactions.SGAgentAction;


/**
 * This {@link burlap.oomdp.stochasticgames.agentactions.SGAgentAction} definition defines a parameter-less agent action
 * that can be
 * executed in every state. This is a useful action definition for symmetric games.
 * @author James MacGlashan
 *
 */
public class UniversalSGAgentAction extends SGAgentAction {

	
	
	/**
	 * Initializes this single action to be for the given domain and with the given name. This action
	 * is automatically added to the given domain
	 * @param d the domain to which this action belongs
	 * @param name the name of this action
	 */
	public UniversalSGAgentAction(SGDomain d, String name) {
		super(d, name);
	}
	



	@Override
	public boolean applicableInState(State s, GroundedSGAgentAction gsa) {
		return true;
	}

}
