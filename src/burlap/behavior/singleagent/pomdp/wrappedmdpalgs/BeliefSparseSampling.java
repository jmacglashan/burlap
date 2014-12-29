package burlap.behavior.singleagent.pomdp.wrappedmdpalgs;

import java.util.List;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy;
import burlap.behavior.singleagent.planning.stochastic.sparsesampling.SparseSampling;
import burlap.behavior.singleagent.pomdp.BeliefMDPPolicyAgent;
import burlap.behavior.singleagent.pomdp.POMDPPlanner;
import burlap.behavior.statehashing.NameDependentStateHashFactory;
import burlap.domain.singleagent.pomdp.tiger.TigerDomain;
import burlap.oomdp.auxiliary.common.NullTermination;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.pomdp.BeliefMDPGenerator;
import burlap.oomdp.singleagent.pomdp.BeliefState;
import burlap.oomdp.singleagent.pomdp.BeliefStatistic;
import burlap.oomdp.singleagent.pomdp.PODomain;
import burlap.oomdp.singleagent.pomdp.POEnvironment;

public class BeliefSparseSampling extends POMDPPlanner implements QComputablePlanner{

	protected SADomain							beliefMDP;
	protected RewardFunction					beliefRF;
	protected SparseSampling					mdpPlanner;
	
	
	public BeliefSparseSampling(PODomain domain, RewardFunction rf, TerminalFunction tf, double discount, int h, int c){
	
		this.plannerInit(domain, rf, tf, discount, new NameDependentStateHashFactory());
		BeliefMDPGenerator bdgen = new BeliefMDPGenerator(domain);
		this.beliefMDP = (SADomain)bdgen.generateDomain();
		this.beliefRF = new BeliefMDPGenerator.BeliefRF(domain, rf);
		
		this.mdpPlanner = new SparseSampling(this.beliefMDP, this.beliefRF, new NullTermination(), discount, new NameDependentStateHashFactory(), h, Math.max(1, c));
		if(c < 1){
			this.mdpPlanner.setComputeExactValueFunction(true);
		}
		
	}
	
	public SADomain getBeliefMDP(){
		return this.beliefMDP;
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
	public void planFromBeliefStatistic(BeliefStatistic bsInput) {
		BeliefState bs = new BeliefState(bsInput.getDomain(),bsInput);
		State s = BeliefMDPGenerator.getBeliefMDPState(this.beliefMDP, bs);
		this.planFromState(s);
	}

	@Override
	public void resetPlannerResults() {
		this.mdpPlanner.resetPlannerResults();
	}

	
	
	public static void main(String [] args){
		TigerDomain tiger = new TigerDomain(true);
		PODomain domain = (PODomain)tiger.generateDomain();
		BeliefState initialBelief = TigerDomain.getInitialBeliefState(domain);
		RewardFunction rf = new TigerDomain.TigerRF();
		TerminalFunction tf = new NullTermination();
		
		BeliefSparseSampling bss = new BeliefSparseSampling(domain, rf, tf, 0.99, 10, -1);
		Policy p = new GreedyQPolicy(bss);
		
		
		
		BeliefMDPPolicyAgent agent = new BeliefMDPPolicyAgent(domain, p);
		agent.setBeliefState(initialBelief);
		
		POEnvironment env = new POEnvironment(domain, rf, tf);
		env.setCurMPDStateTo(TigerDomain.tigerLeftState(domain));
		
		agent.setEnvironment(env);
		
		/*
		State initialBeliefStateOb = BeliefMDPGenerator.getBeliefMDPState(bss.getBeliefMDP(), initialBelief);
		List<QValue> qs = bss.getQs(initialBeliefStateOb);
		for(QValue q : qs){
			System.out.println(q.a.toString() + ": " + q.q);
		}
		*/
		
		
		EpisodeAnalysis ea = agent.actUntilTerminalOrMaxSteps(30);
		
		for(int i = 0; i < ea.numTimeSteps()-1; i++){
			System.out.println(ea.getAction(i) + " " + ea.getReward(i+1));
		}
		
		
	}
	
}
