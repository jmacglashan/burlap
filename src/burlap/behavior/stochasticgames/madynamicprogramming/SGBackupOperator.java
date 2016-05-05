package burlap.behavior.stochasticgames.madynamicprogramming;

import java.util.Map;

import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.SGAgentType;


/**
 * A stochastic games backup operator to be used in multi-agent Q-learning or value function planning. This operator
 * is meant to be applied to a next state, takes the set of Q-values for that state for all agents and returns
 * the backup operator. The classic Bellman MDP approach would be o use a max operator, but in stochastic games,
 * different solution concepts require different operators.
 * @author James MacGlashan; adapted from code written by Esha Gosh John Meehan and Michalis Michaelidis
 *
 */
public interface SGBackupOperator {

	public double performBackup(State s, String forAgent, Map<String, SGAgentType> agentDefinitions, AgentQSourceMap qSourceMap);
	
}
