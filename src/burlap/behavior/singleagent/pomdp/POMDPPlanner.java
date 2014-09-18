package burlap.behavior.singleagent.pomdp;

import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.pomdp.BeliefMDPGenerator;
import burlap.oomdp.singleagent.pomdp.BeliefState;
import burlap.oomdp.singleagent.pomdp.PODomain;

public abstract class POMDPPlanner extends OOMDPPlanner {

	
	/**
	 * Plans from an initial belief MDP state.
	 *
	 * @param  s an input belief MDP state
	 */
	@Override
	public void planFromState(State initialState) {
		
		BeliefState bs = this.getBeliefState(initialState);
		this.planFromBeliefState(bs);

	}
	
	/**
	 * Plan from a given belief state.
	 *
	 * @param  bs input normalized belief state object.
	 */
	public abstract void planFromBeliefState(BeliefState bs);

	/**
	 * Returns a belief state given an input belief MDP state.
	 *
	 * @param  s an input belief MDP state
	 */
	public BeliefState getBeliefState(State s){
		BeliefState bs = new BeliefState((PODomain)this.domain);
		bs.setBeliefVector(s.getFirstObjectOfClass(BeliefMDPGenerator.CLASSBELIEF).getDoubleArrayValue(BeliefMDPGenerator.ATTBELIEF));
		return bs;
	}

}
