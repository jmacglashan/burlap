package burlap.oomdp.singleagent.common;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.statehashing.HashableState;
import burlap.oomdp.statehashing.HashableStateFactory;

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
	protected HashableStateFactory hashingFactory;

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
	 *            the {@link burlap.behavior.statehashing.HashableStateFactory} to
	 *            use for indexing states
	 * @param rewardFunction
	 *            the source reward function that will be lazily cached.
	 */
	public CachedRewardFunction(HashableStateFactory hashingFactory,
			RewardFunction rewardFunction) {
		this.hashingFactory = hashingFactory;
		this.rewardFunction = rewardFunction;
	}

	/**
	 * Initializes
	 * 
	 * @param hashingFactory
	 *            the {@link burlap.behavior.statehashing.HashableStateFactory} to
	 *            use for indexing states
	 * @param rewardFunction
	 *            the source reward function that will be lazily cached.
	 * @param cacheCapacity
	 *            the initial memory capacity to be set aside for the policy
	 *            cache
	 */
	public CachedRewardFunction(HashableStateFactory hashingFactory,
			RewardFunction rewardFunction, int cacheCapacity) {
		this.hashingFactory = hashingFactory;
		this.rewardFunction = rewardFunction;
		this.rewardValue = new HashMap<Transition, Double>(cacheCapacity);
	}

	@Override
	public double reward(State s, GroundedAction a, State sprime) {
		HashableState sh = this.hashingFactory.hashState(s);
		HashableState sprimeh = this.hashingFactory.hashState(sprime);
		Transition transition = new Transition(sh, sprimeh, a);
		Double value = this.rewardValue.get(transition);
		if (value == null) {
			value = this.rewardFunction.reward(s, a, sprime);
			this.rewardValue.put(transition, value);
		}
		return value;
	}

	protected class Transition {
		private final HashableState sh, sprimeh;
		private final GroundedAction a;

		public HashableState getSh() {
			return sh;
		}

		public HashableState getSprimeh() {
			return sprimeh;
		}

		public GroundedAction getA() {
			return a;
		}

		public Transition(HashableState sh, HashableState sprimeh,
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
