package burlap.behavior.singleagent.pomdp.qmdp;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.auxiliary.StateEnumerator;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.behavior.singleagent.pomdp.POMDPPlanner;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.pomdp.BeliefMDPGenerator;
import burlap.oomdp.singleagent.pomdp.BeliefState;
import burlap.oomdp.singleagent.pomdp.BeliefState.StateBelief;
import burlap.oomdp.singleagent.pomdp.BeliefStatistic;
import burlap.oomdp.singleagent.pomdp.PODomain;

public class QMDP extends POMDPPlanner implements QComputablePlanner {

	protected QComputablePlanner mdpQSource;
	
	public QMDP(PODomain domain, QComputablePlanner mdpQSource){
		this.mdpQSource = mdpQSource;
		OOMDPPlanner planner = (OOMDPPlanner)this.mdpQSource;
		this.plannerInit(domain, planner.getRF(), planner.getTF(), planner.getGamme(), planner.getHashingFactory());
	}
	
	public QMDP(PODomain domain, RewardFunction rf, TerminalFunction tf, double discount, StateHashFactory hashingFactory, double maxDelta, int maxIterations){
		this.domain = domain;
		ValueIteration vi = new ValueIteration(domain, rf, tf, discount, hashingFactory, maxDelta, maxIterations);
		this.mdpQSource = vi;
		this.plannerInit(domain, rf, tf, discount, hashingFactory);
	}
	
	public void forceMDPPlanningFromAllStates(){
		OOMDPPlanner planner = (OOMDPPlanner)this.mdpQSource;
		StateEnumerator senum = ((PODomain)this.domain).getStateEnumerator();
		for(int i = 0; i < senum.numStatesEnumerated(); i++){
			State s = senum.getStateForEnumertionId(i);
			planner.planFromState(s);
		}
	}
	
	@Override
	public List<QValue> getQs(State s) {
		
		//get actions for any underlying MDP state
		List<GroundedAction> gas = Action.getAllApplicableGroundedActionsFromActionList(this.domain.getActions(), ((PODomain)this.domain).getStateEnumerator().getStateForEnumertionId(0));
		List<QValue> result = new ArrayList<QValue>(gas.size());
		
		BeliefState bs = new BeliefState((PODomain)this.domain);
		bs.setBeliefCollection(s.getFirstObjectOfClass(BeliefMDPGenerator.CLASSBELIEF).getDoubleArrayValue(BeliefMDPGenerator.ATTBELIEF));
		List<StateBelief> beliefs = bs.getStatesAndBeliefsWithNonZeroProbability();
		
		for(GroundedAction ga : gas){
			double q = this.qForBeliefList(beliefs, ga);
			QValue Q = new QValue(s, ga, q);
			result.add(Q);
		}
		
		return result;
	}

	@Override
	public QValue getQ(State s, AbstractGroundedAction a) {
		
		BeliefState bs = new BeliefState((PODomain)this.domain);
		bs.setBeliefCollection(s.getFirstObjectOfClass(BeliefMDPGenerator.CLASSBELIEF).getDoubleArrayValue(BeliefMDPGenerator.ATTBELIEF));
		QValue q = new QValue(s, a, this.qForBelief(bs, (GroundedAction)a));
		
		return q;
	}
	
	public double qForBelief(BeliefState bs, GroundedAction ga){
		
		List<StateBelief> beliefs = bs.getStatesAndBeliefsWithNonZeroProbability();
		return this.qForBeliefList(beliefs, ga);

	}
	
	protected double qForBeliefList(List<StateBelief> beliefs, GroundedAction ga){
		double q = 0.;
		for(StateBelief sb : beliefs){
			q += sb.belief * this.mdpQSource.getQ(sb.s, ga).q;
		}
		return q;
	}

	@Override
	public void planFromState(State initialState) {
		//do nothing
		
	}

	@Override
	public void resetPlannerResults() {
		//do nothing
	}

	@Override
	public void planFromBeliefStatistic(BeliefStatistic bsInput) {
		this.forceMDPPlanningFromAllStates();
	}

}
