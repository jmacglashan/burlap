package burlap.mdp.stochasticgames.agent;

/**
 * An interface for generating agents
 * @author James MacGlashan
 *
 */
public interface AgentFactory {
	/**
	 * Returns a new agent instance.
	 * @return a new agent instance.
	 */
	SGAgent generateAgent();
}
