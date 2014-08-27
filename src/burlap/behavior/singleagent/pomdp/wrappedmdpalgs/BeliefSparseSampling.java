package burlap.behavior.singleagent.pomdp.wrappedmdpalgs;

import java.util.List;

import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.behavior.singleagent.planning.stochastic.sparsesampling.SparseSampling;
import burlap.behavior.singleagent.pomdp.POMDPPlanner;
import burlap.behavior.statehashing.NameDependentStateHashFactory;
import burlap.oomdp.auxiliary.common.NullTermination;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.pomdp.BeliefMDPGenerator;
import burlap.oomdp.singleagent.pomdp.BeliefState;
import burlap.oomdp.singleagent.pomdp.PODomain;

public class BeliefSparseSampling extends POMDPPlanner implements QComputablePlanner{

	protected SADomain							beliefMDP;
	protected RewardFunction					beliefRF;
	protected SparseSampling					mdpPlanner;
	
	
	public BeliefSparseSampling(PODomain domain, RewardFunction rf, TerminalFunction tf, double discount, int h, int c){
	
		this.plannerInit(domain, rf, tf, discount, new NameDependentStateHashFactory());
		BeliefMDPGenerator bdgen = new BeliefMDPGenerator(domain);
		this.beliefMDP = (SADomain)bdgen.generateDomain();
		this.beliefRF = new BeliefMDPGenerator.BeliefRF(domain, rf);
		
		this.mdpPlanner = new SparseSampling(this.beliefMDP, this.beliefRF, new NullTermination(), discount, new NameDependentStateHashFactory(), h, c);
		if(c < 1){
			this.mdpPlanner.setComputeExactValueFunction(true);
		}
		
	}

	@Override
	public List<QValue> getQs(State s) {
		return this.mdpPlanner.getQs(s);
	}

	@Override
	public QValue getQ(State s, AbstractGroundedAction a) {
		return this.mdpPlanner.getQ(s, a);
	}
	
	@Override
	public void planFromState(State initialState){
		this.mdpPlanner.planFromState(initialState);
	}

	@Override
	public void planFromBeliefState(BeliefState bs) {
		State s = BeliefMDPGenerator.getBeliefMDPState(this.beliefMDP, bs);
		this.planFromState(s);
	}

	@Override
	public void resetPlannerResults() {
		this.mdpPlanner.resetPlannerResults();
	}

}
