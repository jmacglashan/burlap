package burlap.behavior.singleagent;

import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;


/**
 * This class is used to store Q-values.
 * @author James MacGlashan
 *
 */
public class QValue {
	public State 				s;
	public GroundedAction		a;
	public double				q;
	
	/**
	 * Creates a Q-value for the given state an action pair with the specified q-value
	 * @param s the state
	 * @param a the action
	 * @param q the initial Q-value
	 */
	public QValue(State s, GroundedAction a, double q){
		this.s = s;
		this.a = a;
		this.q = q;
	}
	
}
