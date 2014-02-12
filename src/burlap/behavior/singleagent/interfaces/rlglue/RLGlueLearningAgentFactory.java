package burlap.behavior.singleagent.interfaces.rlglue;

import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.RewardFunction;

/**
 * An interface for defining a learning agent factory that can take as inptu an RLGlue generated domain, reward function,
 * terminal function, and discount factor and generated a relevant BURLAP learning agent.
 * @author James MacGlashan
 *
 */
public interface RLGlueLearningAgentFactory {
	
	/**
	 * Returns a BURLAP learning agent for the given RLGlue generated domain, discount, reward function, and terminal function.
	 * @param domain a BURALP domain that wraps the RLGlue environment and task
	 * @param discount the RLGlue specified discount factor
	 * @param rf the RLGlue specified reward function
	 * @param tf the RLGlue specified terminal function
	 * @return a BURLAP learning agent
	 */
	public LearningAgent generateAgentForRLDomain(Domain domain, double discount, RewardFunction rf, TerminalFunction tf);
}
