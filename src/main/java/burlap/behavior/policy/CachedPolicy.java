package burlap.behavior.policy;

import burlap.behavior.policy.support.ActionProb;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.HashableState;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class can be used to lazily cache the policy of a source policy. This is useful when the source policy may
 * require some degree of computation and you'd like to store the results of that computation to memory so that
 * it does not have be recomputed for states that have their policy queried multiple times.
 *
 * @author James MacGlashan.
 */
public class CachedPolicy implements EnumerablePolicy{

	/**
	 * The hashing factory to use for indexing states
	 */
	protected HashableStateFactory hashingFactory;

	/**
	 * The cached action selection probabilities
	 */
	protected Map<HashableState, List<ActionProb>> actionSelection = new HashMap<HashableState, List<ActionProb>>();

	/**
	 * The source policy that gets cached
	 */
	protected EnumerablePolicy sourcePolicy;


	/**
	 * Initializes
	 * @param hashingFactory the {@link burlap.statehashing.HashableStateFactory} to use for indexing states
	 * @param sourcePolicy the source policy that will be lazily cached.
	 */
	public CachedPolicy(HashableStateFactory hashingFactory, EnumerablePolicy sourcePolicy) {
		this.hashingFactory = hashingFactory;
		this.sourcePolicy = sourcePolicy;
	}


	/**
	 * Initializes
	 * @param hashingFactory the {@link burlap.statehashing.HashableStateFactory} to use for indexing states
	 * @param sourcePolicy the source policy that will be lazily cached.
	 * @param cacheCapacity the initial memory capacity to be set aside for the policy cache
	 */
	public CachedPolicy(HashableStateFactory hashingFactory, EnumerablePolicy sourcePolicy, int cacheCapacity) {
		this.hashingFactory = hashingFactory;
		this.sourcePolicy = sourcePolicy;
		this.actionSelection = new HashMap<HashableState, List<ActionProb>>(cacheCapacity);
	}

	@Override
	public Action action(State s) {
		return PolicyUtils.sampleFromActionDistribution(this, s);
	}

	@Override
	public double actionProb(State s, Action a) {
		return PolicyUtils.actionProbFromEnum(this, s, a);
	}

	@Override
	public List<ActionProb> policyDistribution(State s) {
		HashableState sh = this.hashingFactory.hashState(s);
		List<ActionProb> aps = this.actionSelection.get(sh);
		if(aps == null){
			aps = this.sourcePolicy.policyDistribution(s);
			this.actionSelection.put(sh, aps);
		}
		return aps;
	}


	@Override
	public boolean definedFor(State s) {
		HashableState sh = this.hashingFactory.hashState(s);
		return this.actionSelection.containsKey(sh) ? true : this.sourcePolicy.definedFor(s);
	}
}
