package burlap.behavior.singleagent.pomdp.wrappedmdpalgs;

import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.MDPSolver;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.singleagent.planning.stochastic.sparsesampling.SparseSampling;
import burlap.behavior.singleagent.pomdp.BeliefPolicyAgent;
import burlap.behavior.valuefunction.QFunction;
import burlap.behavior.valuefunction.QValue;
import burlap.domain.singleagent.pomdp.tiger.TigerDomain;
import burlap.domain.singleagent.pomdp.tiger.TigerState;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.pomdp.BeliefMDPGenerator;
import burlap.mdp.singleagent.pomdp.PODomain;
import burlap.mdp.singleagent.pomdp.SimulatedPOEnvironment;
import burlap.mdp.singleagent.pomdp.beliefstate.BeliefState;
import burlap.mdp.singleagent.pomdp.beliefstate.tabular.HashableTabularBeliefStateFactory;
import burlap.statehashing.HashableStateFactory;

import java.util.List;


/**
 * A POMDP planning algorithm that converts a POMDP into a Belief MDP and then uses {@link burlap.behavior.singleagent.planning.stochastic.sparsesampling.SparseSampling}
 * to solve it. If the full transition dynamics are used (set c in the constructor to -1), then it provides and optimal finite horizon POMDP policy.
 */
public class BeliefSparseSampling extends MDPSolver implements Planner, QFunction{

	/**
	 * The belief MDP domain to solve.
	 */
	protected SADomain							beliefMDP;


	/**
	 * The {@link burlap.behavior.singleagent.planning.stochastic.sparsesampling.SparseSampling} planning instance to solve the problem.
	 */
	protected SparseSampling					mdpPlanner;


	/**
	 * Initializes the planner.
	 * @param domain the POMDP domain
	 * @param discount the discount factor
	 * @param hashingFactory the Belief MDP {@link burlap.statehashing.HashableStateFactory} that {@link burlap.behavior.singleagent.planning.stochastic.sparsesampling.SparseSampling} will use.
	 * @param h the height of the {@link burlap.behavior.singleagent.planning.stochastic.sparsesampling.SparseSampling} tree.
	 * @param c the number of samples {@link burlap.behavior.singleagent.planning.stochastic.sparsesampling.SparseSampling} will use. Set to -1 to use the full BeliefMDP transition dynamics.
	 */
	public BeliefSparseSampling(PODomain domain, double discount, HashableStateFactory hashingFactory, int h, int c){
	
		this.solverInit(domain, discount, hashingFactory);
		BeliefMDPGenerator bdgen = new BeliefMDPGenerator(domain);
		this.beliefMDP = (SADomain)bdgen.generateDomain();
		
		this.mdpPlanner = new SparseSampling(this.beliefMDP, discount, hashingFactory, h, Math.max(1, c));
		if(c < 1){
			this.mdpPlanner.setComputeExactValueFunction(true);
		}
		
	}


	/**
	 * Returns the generated Belief MDP that will be solved.
	 * @return the generated Belief MDP that will be solved.
	 */
	public SADomain getBeliefMDP(){
		return this.beliefMDP;
	}

	/**
	 * Returns the {@link burlap.behavior.singleagent.planning.stochastic.sparsesampling.SparseSampling} planning used to solve the Belief MDP.
	 * @return the {@link burlap.behavior.singleagent.planning.stochastic.sparsesampling.SparseSampling} planning used to solve the Belief MDP.
	 */
	public SparseSampling getSparseSamplingPlanner(){
		return this.mdpPlanner;
	}

	@Override
	public List<QValue> getQs(State s) {
		return this.mdpPlanner.getQs(s);
	}

	@Override
	public QValue getQ(State s, Action a) {
		return this.mdpPlanner.getQ(s, a);
	}
	
	@Override
	public Policy planFromState(State initialState){
		this.mdpPlanner.planFromState(initialState);
		return new GreedyQPolicy(this);
	}

	@Override
	public void resetSolver() {
		this.mdpPlanner.resetSolver();
	}

	@Override
	public double value(State s) {
		return QFunctionHelper.getOptimalValue(this, s);
	}


	
	
	public static void main(String [] args){
		TigerDomain tiger = new TigerDomain(true);
		PODomain domain = (PODomain)tiger.generateDomain();
		BeliefState initialBelief = TigerDomain.getInitialBeliefState(domain);
		
		BeliefSparseSampling bss = new BeliefSparseSampling(domain, 0.99, new HashableTabularBeliefStateFactory(), 10, -1);
		Policy p = new GreedyQPolicy(bss);

		SimulatedPOEnvironment env = new SimulatedPOEnvironment(domain);
		env.setCurStateTo(new TigerState(TigerDomain.VAL_LEFT));
		
		BeliefPolicyAgent agent = new BeliefPolicyAgent(domain, env, p);
		agent.setBeliefState(initialBelief);
		

		
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
