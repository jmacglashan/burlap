package burlap.behavior.stochasticgames.auxiliary.performance;

import burlap.oomdp.stochasticgames.AgentFactory;
import burlap.oomdp.stochasticgames.SGAgentType;

/**
 * A pair storing an agent factory and the agent type that the generated agent will join the world as.
 * @author James MacGlashan
 *
 */
public class AgentFactoryAndType {
	public AgentFactory agentFactory;
	public SGAgentType at;
	
	/**
	 * Initializes
	 * @param agentFactory the agent factory
	 * @param at the agent type the agent will join a world as
	 */
	public AgentFactoryAndType(AgentFactory agentFactory, SGAgentType at){
		this.agentFactory = agentFactory;
		this.at = at;
	}
	
}
