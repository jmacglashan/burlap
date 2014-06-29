package burlap.behavior.singleagent.pomdp;

import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.pomdp.BeliefMDPGenerator;
import burlap.oomdp.singleagent.pomdp.BeliefState;
import burlap.oomdp.singleagent.pomdp.PODomain;

public abstract class POMDPPlanner extends OOMDPPlanner {

	
	@Override
	public void planFromState(State initialState) {
		
		BeliefState bs = this.getBeliefState(initialState);
		this.planFromBeliefState(bs);

	}
	
	public abstract void planFromBeliefState(BeliefState bs);

	
	public BeliefState getBeliefState(State s){
		BeliefState bs = new BeliefState((PODomain)this.domain);
		bs.setBeliefVector(s.getFirstObjectOfClass(BeliefMDPGenerator.CLASSBELIEF).getDoubleArrayValue(BeliefMDPGenerator.ATTBELIEF));
		return bs;
	}

}
