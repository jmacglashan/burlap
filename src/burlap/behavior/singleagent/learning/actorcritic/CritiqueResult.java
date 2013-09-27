package burlap.behavior.singleagent.learning.actorcritic;

import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;

public class CritiqueResult {

	protected State					s;
	protected GroundedAction		a;
	protected State					sprime;
	protected double				critique;
	
	public CritiqueResult(State s, GroundedAction a, State sprime, double critique) {
		this.s = s;
		this.a = a;
		this.sprime = sprime;
		this.critique = critique;
	}

	public State getS() {
		return s;
	}

	public GroundedAction getA() {
		return a;
	}

	public State getSprime() {
		return sprime;
	}

	public double getCritique() {
		return critique;
	}

	
	
}
