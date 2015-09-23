package burlap.oomdp.singleagent.environment;

import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.RewardFunction;

/**
 * And {@link burlap.oomdp.singleagent.environment.Environment} interface extension that allows the {@link burlap.oomdp.singleagent.RewardFunction}
 * and {@link burlap.oomdp.core.TerminalFunction} to set and accessed.
 * @author James MacGlashan.
 */
public interface TaskSettableEnvironment extends Environment{

	/**
	 * Sets the {@link burlap.oomdp.singleagent.RewardFunction} of this {@link burlap.oomdp.singleagent.environment.Environment} to
	 * the specified reward function.
	 * @param rf the new {@link burlap.oomdp.singleagent.RewardFunction} of the {@link burlap.oomdp.singleagent.environment.Environment}.
	 */
	void setRf(RewardFunction rf);

	/**
	 * Sets the {@link burlap.oomdp.core.TerminalFunction} of this {@link burlap.oomdp.singleagent.environment.Environment} to
	 * the specified terminal function.
	 * @param tf the new {@link burlap.oomdp.core.TerminalFunction} of the {@link burlap.oomdp.singleagent.environment.Environment}.
	 */
	void setTf(TerminalFunction tf);

	/**
	 * Returns the {@link burlap.oomdp.singleagent.RewardFunction} this {@link burlap.oomdp.singleagent.environment.Environment} uses
	 * to determine rewards.
	 * @return a {@link burlap.oomdp.singleagent.RewardFunction}
	 */
	RewardFunction getRf();

	/**
	 * Returns the {@link burlap.oomdp.core.TerminalFunction} this {@link burlap.oomdp.singleagent.environment.Environment} uses
	 * to determine terminal states
	 * @return a {@link burlap.oomdp.core.TerminalFunction}
	 */
	TerminalFunction getTf();

}
