package burlap.behavior.singleagent.planning;

import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.core.states.State;

/**
 * An analog to the {@link burlap.oomdp.core.TransitionProbability}, except it stores {@link burlap.behavior.statehashing.StateHashTuple} objects
 * instead of {@link burlap.oomdp.core.states.State} objects.
 * @author James MacGlashan
 *
 */
public class HashedTransitionProbability {

	public StateHashTuple sh;
	public double p;
	
	
	/**
	 * Initializes with a {@link burlap.behavior.statehashing.StateHashTuple} and probability for the transition
	 * @param sh the hashed state that the agent transitions to
	 * @param p the probability of the transition
	 */
	public HashedTransitionProbability(StateHashTuple sh, double p){
		this.sh = sh;
		this.p = p;
	}
	
	
	/**
	 * Takes a {@link burlap.oomdp.core.states.State} object, hashes it, and sets the transition probability to the hashed state to p
	 * @param s the state that the agent transitions to
	 * @param p the probability of the transition
	 * @param hashingFactory the hashing factory to use to hash the input state
	 */
	public HashedTransitionProbability(State s, double p, StateHashFactory hashingFactory){
		this.sh = hashingFactory.hashState(s);
		this.p = p;
	}
	
	
	/**
	 * Takes a {@link burlap.oomdp.core.TransitionProbability} and hashes its state using the hashingFactory object
	 * @param tp the {@link burlap.oomdp.core.TransitionProbability} to hash
	 * @param hashingFactory the hashing factory to use.
	 */
	public HashedTransitionProbability(TransitionProbability tp, StateHashFactory hashingFactory){
		this.sh = hashingFactory.hashState(tp.s);
		this.p = tp.p;
	}
	
}
