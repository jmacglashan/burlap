package burlap.mdp.auxiliary.common;

import burlap.behavior.singleagent.auxiliary.StateReachability;
import burlap.mdp.auxiliary.StateGenerator;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.statehashing.HashableStateFactory;
import burlap.statehashing.simple.SimpleHashableStateFactory;

import java.util.List;
import java.util.Random;


/**
 * This class will return a random state from a set of states that are reachable from a source seed state.
 * 
 * @author Stephen Brawner and Mark Ho. Documented by James MacGlashan
 *
 */
public class RandomStartStateGenerator implements StateGenerator {

	private List<State> reachableStates;
	private Random 		random;

	/**
	 * Will discover the reachable states from which to randomly select. Reachable states found using a {@link SimpleHashableStateFactory} with identifier dependence.
	 * @param domain the domain from which states will be drawn.
	 * @param seedState the seed state from which the reachable states will be found.
	 */
	public RandomStartStateGenerator(SADomain domain, State seedState) {
		HashableStateFactory hashFactory = new SimpleHashableStateFactory(false);
		this.reachableStates = StateReachability.getReachableStates(seedState, domain, hashFactory);
		this.random = new Random();
	}
	
	
	/**
	 * Will discover reachable states from which to randomly select.
	 * @param domain the domain from which states will be drawn.
	 * @param seedState the seed state from which the reachable states will be found.
	 * @param hashFactory the hash factory to use for the reachability analysis.
	 */
	public RandomStartStateGenerator(SADomain domain, State seedState, HashableStateFactory hashFactory) {
		this.reachableStates = StateReachability.getReachableStates(seedState, domain, hashFactory);
		this.random = new Random();
	}

	@Override
	public State generateState() {
		return this.reachableStates.get(this.random.nextInt(this.reachableStates.size()));
	}

}
