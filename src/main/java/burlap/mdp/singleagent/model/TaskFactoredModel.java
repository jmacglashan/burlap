package burlap.mdp.singleagent.model;

import burlap.mdp.core.TerminalFunction;

/**
 * An interface for a {@link SampleModel} that computes its rewards and terminal states using {@link RewardFunction} and
 * {@link TerminalFunction} objects that can be modified.
 * @author James MacGlashan.
 */
public interface TaskFactoredModel extends SampleModel{

	/**
	 * Tells this model to use the corresponding {@link RewardFunction}
	 * @param rf the {@link RewardFunction}
	 */
	void useRewardFunction(RewardFunction rf);

	/**
	 * Tells this model to use the corresponding {@link TerminalFunction}
	 * @param tf the {@link TerminalFunction}
	 */
	void useTerminalFunction(TerminalFunction tf);

	/**
	 * Returns the {@link RewardFunction} this model uses to compute rewards
	 * @return the {@link RewardFunction} this model uses to compute rewards
	 */
	RewardFunction rewardFunction();

	/**
	 * Returns the {@link TerminalFunction} this model uses to determine terminal states
	 * @return the {@link TerminalFunction} this model uses to determine terminal states
	 */
	TerminalFunction terminalFunction();
}
