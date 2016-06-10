package burlap.mdp.stochasticgames.action;

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
