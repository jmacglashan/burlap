package burlap.behavior.policy;

import burlap.behavior.statehashing.HashableStateFactory;
import burlap.behavior.statehashing.HashableState;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.states.State;

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
public class CachedPolicy extends Policy{

	/**
	 * The hashing factory to use for indexing states
	 */
	protected HashableStateFactory hashingFactory;

	/**
	 * The cached action selection probabilities
	 */
	protected Map<HashableState, List<Policy.ActionProb>> actionSelection = new HashMap<HashableState, List<Policy.ActionProb>>();

	/**
	 * The source policy that gets cached
	 */
	protected Policy sourcePolicy;


	/**
	 * Initializes
	 * @param hashingFactory the {@link burlap.behavior.statehashing.HashableStateFactory} to use for indexing states
	 * @param sourcePolicy the source policy that will be lazily cached.
	 */
	public CachedPolicy(HashableStateFactory hashingFactory, Policy sourcePolicy) {
		this.hashingFactory = hashingFactory;
		this.sourcePolicy = sourcePolicy;
	}


	/**
	 * Initializes
	 * @param hashingFactory the {@link burlap.behavior.statehashing.HashableStateFactory} to use for indexing states
	 * @param sourcePolicy the source policy that will be lazily cached.
	 * @param cacheCapacity the initial memory capacity to be set aside for the policy cache
	 */
	public CachedPolicy(HashableStateFactory hashingFactory, Policy sourcePolicy, int cacheCapacity) {
		this.hashingFactory = hashingFactory;
		this.sourcePolicy = sourcePolicy;
		this.actionSelection = new HashMap<HashableState, List<ActionProb>>(cacheCapacity);
	}

	@Override
	public AbstractGroundedAction getAction(State s) {
		return this.sampleFromActionDistribution(s);
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		HashableState sh = this.hashingFactory.hashState(s);
		List<Policy.ActionProb> aps = this.actionSelection.get(sh);
		if(aps == null){
			aps = this.sourcePolicy.getActionDistributionForState(s);
			this.actionSelection.put(sh, aps);
		}
		return aps;
	}

	@Override
	public boolean isStochastic() {
		return this.sourcePolicy.isStochastic();
	}

	@Override
	public boolean isDefinedFor(State s) {
		HashableState sh = this.hashingFactory.hashState(s);
		return this.actionSelection.containsKey(sh) ? true : this.sourcePolicy.isDefinedFor(s);
	}
}
