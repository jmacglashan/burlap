package burlap.behavior.singleagent.planning.commonpolicies;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;

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
	protected StateHashFactory hashingFactory;

	/**
	 * The cached action selection probabilities
	 */
	protected Map<StateHashTuple, List<Policy.ActionProb>> actionSelection = new HashMap<StateHashTuple, List<Policy.ActionProb>>();

	/**
	 * The source policy that gets cached
	 */
	protected Policy sourcePolicy;


	/**
	 * Initializes
	 * @param hashingFactory the {@link burlap.behavior.statehashing.StateHashFactory} to use for indexing states
	 * @param sourcePolicy the source policy that will be lazily cached.
	 */
	public CachedPolicy(StateHashFactory hashingFactory, Policy sourcePolicy) {
		this.hashingFactory = hashingFactory;
		this.sourcePolicy = sourcePolicy;
	}


	/**
	 * Initializes
	 * @param hashingFactory the {@link burlap.behavior.statehashing.StateHashFactory} to use for indexing states
	 * @param sourcePolicy the source policy that will be lazily cached.
	 * @param cacheCapacity the initial memory capacity to be set aside for the policy cache
	 */
	public CachedPolicy(StateHashFactory hashingFactory, Policy sourcePolicy, int cacheCapacity) {
		this.hashingFactory = hashingFactory;
		this.sourcePolicy = sourcePolicy;
		this.actionSelection = new HashMap<StateHashTuple, List<ActionProb>>(cacheCapacity);
	}

	@Override
	public AbstractGroundedAction getAction(State s) {
		return this.sampleFromActionDistribution(s);
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		StateHashTuple sh = this.hashingFactory.hashState(s);
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
		StateHashTuple sh = this.hashingFactory.hashState(s);
		return this.actionSelection.containsKey(sh) ? true : this.sourcePolicy.isDefinedFor(s);
	}
}
