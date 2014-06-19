package burlap.behavior.stochasticgame.auxiliary.performance;

import burlap.oomdp.stochasticgames.AgentFactory;
import burlap.oomdp.stochasticgames.AgentType;

/**
 * A pair storing an agent factory and the agent type that the generated agent will join the world as.
 * @author James MacGlashan
 *
 */
public class AgentFactoryAndType {
	public AgentFactory agentFactory;
	public AgentType at;
	
	/**
	 * Initializes
	 * @param agentFactory the agent factory
	 * @param at the agent type the agent will join a world as
	 */
	public AgentFactoryAndType(AgentFactory agentFactory, AgentType at){
		this.agentFactory = agentFactory;
		this.at = at;
	}
	
}
