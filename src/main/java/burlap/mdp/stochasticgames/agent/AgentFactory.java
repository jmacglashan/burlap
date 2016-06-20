package burlap.mdp.stochasticgames.agent;

/**
 * An interface for generating agents
 * @author James MacGlashan
 *
 */
public interface AgentFactory {

	/**
	 * Generates a new {@link SGAgent}
	 * @param agentName the name for the agent
	 * @param type the {@link SGAgentType} for the agent
	 * @return a new {@link SGAgent}
	 */
	SGAgent generateAgent(String agentName, SGAgentType type);
}
