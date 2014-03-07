package burlap.behavior.stochasticgame.agents.interfacing.singleagent;

import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.RewardFunction;

public interface SALearningAgentFactoryForSG {

	/**
	 * Returns a BURLAP learning agent for the given RLGlue generated domain, discount, reward function, and terminal function.
	 * @param domain a BURALP domain that wraps the RLGlue environment and task
	 * @param rf the RLGlue specified reward function
	 * @param tf the RLGlue specified terminal function
	 * @return a BURLAP learning agent
	 */
	public LearningAgent generateAgentForRLDomain(Domain domain, RewardFunction rf, TerminalFunction tf);
	
}
