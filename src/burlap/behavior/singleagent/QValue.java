package burlap.behavior.singleagent;

import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;

public class QValue {
	public State 				s;
	public GroundedAction		a;
	public double				q;
	
	public QValue(State s, GroundedAction a, double q){
		this.s = s;
		this.a = a;
		this.q = q;
	}
	
}
