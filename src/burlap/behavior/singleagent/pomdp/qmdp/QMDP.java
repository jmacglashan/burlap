package burlap.behavior.singleagent.pomdp.qmdp;

import java.util.List;

import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.auxiliary.StateEnumerator;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.pomdp.PODomain;

public class QMDP extends OOMDPPlanner implements QComputablePlanner {

	protected QComputablePlanner mdpQSource;
	
	public QMDP(PODomain domain, QComputablePlanner mdpQSource){
		this.mdpQSource = mdpQSource;
		OOMDPPlanner planner = (OOMDPPlanner)this.mdpQSource;
		this.plannerInit(domain, planner.getRF(), planner.getTF(), 0.99, null);
	}
	
	public QMDP(PODomain domain, RewardFunction rf, TerminalFunction tf, double discount, StateHashFactory hashingFactory, double maxDelta, int maxIterations){
		this.domain = domain;
		ValueIteration vi = new ValueIteration(domain, rf, tf, discount, hashingFactory, maxDelta, maxIterations);
		this.mdpQSource = vi;
		this.forceMDPPlanningOnAllStates();
	}
	
	public void forceMDPPlanningOnAllStates(){
		OOMDPPlanner planner = (OOMDPPlanner)this.mdpQSource;
		StateEnumerator senum = ((PODomain)this.domain).getStateEnumerator();
		for(int i = 0; i < senum.numStatesEnumerated(); i++){
			State s = senum.getStateForEnumertionId(i);
			planner.planFromState(s);
		}
	}
	
	@Override
	public List<QValue> getQs(State s) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QValue getQ(State s, AbstractGroundedAction a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void planFromState(State initialState) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resetPlannerResults() {
		// TODO Auto-generated method stub
		
	}

}
