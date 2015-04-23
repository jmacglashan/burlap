package burlap.oomdp.core;

import burlap.oomdp.core.states.State;


/**
 * Represents the probability of transition to a given state.
 * @author James MacGlashan
 *
 */
public class TransitionProbability {

	/**
	 * The state to which the agent may transition.
	 */
	public State		s;
	
	/**
	 * the probability of transitioning to state s
	 */
	public double		p;
	
	public TransitionProbability(State s, double p){
		this.s = s;
		this.p = p;
	}
	
}
