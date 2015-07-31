package burlap.oomdp.singleagent.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jfree.data.ComparableObjectItem;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.Policy.ActionProb;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

/**
 * This class can be used to lazily cache the reward of a source reward
 * function. This is useful when the source reward function may require some
 * degree of computation and you'd like to store the results of that computation
 * to memory so that it does not have be recomputed for transitions that have
 * their reward queried multiple times.
 *
 * @author Carl Trimbach.
 */

public class CachedRewardFunction implements RewardFunction {

	/**
	 * The hashing factory to use for indexing states
	 */
	protected StateHashFactory hashingFactory;

	/**
	 * The cached rewards
	 */
	protected Map<Transition, Double> rewardValue = new HashMap<Transition, Double>();

	/**
	 * The source reward function that gets cached
	 */
	protected RewardFunction rewardFunction;

	/**
	 * Initializes
	 * 
	 * @param hashingFactory
	 *            the {@link burlap.behavior.statehashing.StateHashFactory} to
	 *            use for indexing states
	 * @param rewardFunction
	 *            the source reward function that will be lazily cached.
	 */
	public CachedRewardFunction(StateHashFactory hashingFactory,
			RewardFunction rewardFunction) {
		this.hashingFactory = hashingFactory;
		this.rewardFunction = rewardFunction;
	}

	/**
	 * Initializes
	 * 
	 * @param hashingFactory
	 *            the {@link burlap.behavior.statehashing.StateHashFactory} to
	 *            use for indexing states
	 * @param rewardFunction
	 *            the source reward function that will be lazily cached.
	 * @param cacheCapacity
	 *            the initial memory capacity to be set aside for the policy
	 *            cache
	 */
	public CachedRewardFunction(StateHashFactory hashingFactory,
			RewardFunction rewardFunction, int cacheCapacity) {
		this.hashingFactory = hashingFactory;
		this.rewardFunction = rewardFunction;
		this.rewardValue = new HashMap<Transition, Double>(cacheCapacity);
	}

	@Override
	public double reward(State s, GroundedAction a, State sprime) {
		StateHashTuple sh = this.hashingFactory.hashState(s);
		StateHashTuple sprimeh = this.hashingFactory.hashState(sprime);
		Transition transition = new Transition(sh, sprimeh, a);
		Double value = this.rewardValue.get(transition);
		if (value == null) {
			value = this.rewardFunction.reward(s, a, sprime);
			this.rewardValue.put(transition, value);
		}
		return value;
	}

	protected class Transition {
		private final StateHashTuple sh, sprimeh;
		private final GroundedAction a;

		public StateHashTuple getSh() {
			return sh;
		}

		public StateHashTuple getSprimeh() {
			return sprimeh;
		}

		public GroundedAction getA() {
			return a;
		}

		public Transition(StateHashTuple sh, StateHashTuple sprimeh,
				GroundedAction a) {
			this.sh = sh;
			this.sprimeh = sprimeh;
			this.a = a;
		}

		@Override
		public boolean equals(Object other) {
			boolean result = false;
			if (other instanceof Transition) {
				Transition that = (Transition) other;
				result = (this.getSh().equals(that.getSh())
						&& this.getSprimeh().equals(that.getSprimeh()) && this
						.getA().equals(that.getA()));
			}
			return result;
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder(17, 31).append(sh).append(sprimeh)
					.append(a).toHashCode();
		}

	}

}
