package burlap.behavior.stochasticgame.mavaluefunction;

import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.JointAction;


/**
 * Class for storing Q-value informartion for a joint action. It is effectively a triple consisting of a state, joint action, and a double for the corresponding q-value.
 * @author James MacGlashan
 *
 */
public class JAQValue {
	public State			s;
	public JointAction		ja;
	public double			q;
	
	public JAQValue(State s, JointAction ja, double q){
		this.s = s;
		this.ja = ja;
		this.q = q;
	}
	
}
