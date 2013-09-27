package burlap.behavior.singleagent.planning;

import java.util.List;
import java.util.Map;

import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TransitionProbability;


public class HashedTransitionProbability {

	public StateHashTuple sh;
	public double p;
	
	public HashedTransitionProbability(StateHashTuple sh, double p){
		this.sh = sh;
		this.p = p;
	}
	
	public HashedTransitionProbability(State s, double p, StateHashFactory hashingFactory){
		this.sh = hashingFactory.hashState(s);
		this.p = p;
	}
	
	public HashedTransitionProbability(TransitionProbability tp, StateHashFactory hashingFactory){
		this.sh = hashingFactory.hashState(tp.s);
		this.p = tp.p;
	}
	
}
