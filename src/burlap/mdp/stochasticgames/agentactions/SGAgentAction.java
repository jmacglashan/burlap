package burlap.mdp.stochasticgames.agentactions;

import burlap.mdp.core.Action;


/**
 *
 *
 * @author James MacGlashan
 *
 */
public interface SGAgentAction extends Action {

	String actingAgent();
	void setActingAgent(String actingAgent);
}
