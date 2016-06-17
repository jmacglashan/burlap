package burlap.behavior.stochasticgames.madynamicprogramming;


/**
 * An interface for an object that can providing the Q-values stored for each agent in a problem.
 * @author James MacGlashan
 *
 */
public interface MultiAgentQSourceProvider {
	
	/**
	 * Returns an object that can provide Q-value sources for each agent.
	 * @return a {@link AgentQSourceMap} object.
	 */
	AgentQSourceMap getQSources();
}
