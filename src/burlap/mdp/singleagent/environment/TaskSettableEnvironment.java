package burlap.mdp.singleagent.environment;

import burlap.mdp.core.TerminalFunction;
import burlap.mdp.singleagent.RewardFunction;

/**
 * And {@link burlap.mdp.singleagent.environment.Environment} interface extension that allows the {@link burlap.mdp.singleagent.RewardFunction}
 * and {@link burlap.mdp.core.TerminalFunction} to set and accessed.
 * @author James MacGlashan.
 */
public interface TaskSettableEnvironment extends Environment{

	/**
	 * Sets the {@link burlap.mdp.singleagent.RewardFunction} of this {@link burlap.mdp.singleagent.environment.Environment} to
	 * the specified reward function.
	 * @param rf the new {@link burlap.mdp.singleagent.RewardFunction} of the {@link burlap.mdp.singleagent.environment.Environment}.
	 */
	void setRf(RewardFunction rf);

	/**
	 * Sets the {@link burlap.mdp.core.TerminalFunction} of this {@link burlap.mdp.singleagent.environment.Environment} to
	 * the specified terminal function.
	 * @param tf the new {@link burlap.mdp.core.TerminalFunction} of the {@link burlap.mdp.singleagent.environment.Environment}.
	 */
	void setTf(TerminalFunction tf);

	/**
	 * Returns the {@link burlap.mdp.singleagent.RewardFunction} this {@link burlap.mdp.singleagent.environment.Environment} uses
	 * to determine rewards.
	 * @return a {@link burlap.mdp.singleagent.RewardFunction}
	 */
	RewardFunction getRf();

	/**
	 * Returns the {@link burlap.mdp.core.TerminalFunction} this {@link burlap.mdp.singleagent.environment.Environment} uses
	 * to determine terminal states
	 * @return a {@link burlap.mdp.core.TerminalFunction}
	 */
	TerminalFunction getTf();

}
