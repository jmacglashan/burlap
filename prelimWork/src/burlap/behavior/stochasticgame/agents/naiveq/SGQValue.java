package burlap.behavior.stochasticgame.agents.naiveq;

import burlap.oomdp.stochasticgames.GroundedSingleAction;


/**
 * A Q-value wrapper for stochastic games
 * @author James MacGlashan
 *
 */
public class SGQValue {

	/**
	 * The action this Q-value is for
	 */
	public GroundedSingleAction			gsa;
	
	/**
	 * The numeric Q-value
	 */
	public double						q;
	
	
	/**
	 * Initializes as a copy and an existing Q-value
	 * @param qv the Q-value object to copy
	 */
	public SGQValue(SGQValue qv) {
		this.gsa = qv.gsa;
		this.q = qv.q;
	}
	
	
	/**
	 * Initializes with a given action an Q-value
	 * @param gsa the action this Q-value is for.
	 * @param q the numeric Q-value.
	 */
	public SGQValue(GroundedSingleAction gsa, double q){
		this.gsa = gsa;
		this.q = q;
	}

}
