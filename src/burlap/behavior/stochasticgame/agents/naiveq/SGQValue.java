package burlap.behavior.stochasticgame.agents.naiveq;

import burlap.oomdp.stocashticgames.GroundedSingleAction;

public class SGQValue {

	public GroundedSingleAction			gsa;
	public double						q;
	
	public SGQValue(SGQValue qv) {
		this.gsa = qv.gsa;
		this.q = qv.q;
	}
	
	public SGQValue(GroundedSingleAction gsa, double q){
		this.gsa = gsa;
		this.q = q;
	}

}
