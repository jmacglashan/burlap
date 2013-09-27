package burlap.oomdp.core;


public class TransitionProbability {

	public State		s;
	public double		p;
	
	public TransitionProbability(State s, double p){
		this.s = s;
		this.p = p;
	}
	
}
