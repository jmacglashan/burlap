package burlap.behavior.singleagent.planning.stochastic;

import burlap.mdp.statehashing.HashableStateFactory;
import burlap.mdp.statehashing.HashableState;
import burlap.mdp.core.state.State;
import burlap.mdp.core.TransitionProbability;

/**
 * An analog to the {@link burlap.mdp.core.TransitionProbability}, except it stores {@link burlap.mdp.statehashing.HashableState} objects
 * instead of {@link State} objects.
 * @author James MacGlashan
 *
 */
public class HashedTransitionProbability {

	public HashableState sh;
	public double p;
	
	
	/**
	 * Initializes with a {@link burlap.mdp.statehashing.HashableState} and probability for the transition
	 * @param sh the hashed state that the agent transitions to
	 * @param p the probability of the transition
	 */
	public HashedTransitionProbability(HashableState sh, double p){
		this.sh = sh;
		this.p = p;
	}
	
	
	/**
	 * Takes a {@link State} object, hashes it, and sets the transition probability to the hashed state to p
	 * @param s the state that the agent transitions to
	 * @param p the probability of the transition
	 * @param hashingFactory the hashing factory to use to hash the input state
	 */
	public HashedTransitionProbability(State s, double p, HashableStateFactory hashingFactory){
		this.sh = hashingFactory.hashState(s);
		this.p = p;
	}
	
	
	/**
	 * Takes a {@link burlap.mdp.core.TransitionProbability} and hashes its state using the hashingFactory object
	 * @param tp the {@link burlap.mdp.core.TransitionProbability} to hash
	 * @param hashingFactory the hashing factory to use.
	 */
	public HashedTransitionProbability(TransitionProbability tp, HashableStateFactory hashingFactory){
		this.sh = hashingFactory.hashState(tp.s);
		this.p = tp.p;
	}
	
}
