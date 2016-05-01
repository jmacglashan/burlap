package burlap.behavior.singleagent.learning.modellearning.modelplanners;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.learning.modellearning.ModelLearningPlanner;
import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.oomdp.statehashing.HashableStateFactory;
import burlap.oomdp.statehashing.HashableState;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.RewardFunction;

/**
 * A model learning interface wrapper to VI that causes VI to be performed every time the model is updated or whenever a novel state is seen
 * that was not previously expected to be reachable. When the model changes, planning is always performed from the initial state
 * of an episode as well as the last changed episode
 * 
 * @author James MacGlashan
 *
 */
public class VIModelLearningPlanner extends ValueIteration implements ModelLearningPlanner {

	
	/**
	 * States the agent has observed during learning.
	 */
	protected Set<HashableState>	observedStates = new HashSet<HashableState>();


	/**
	 * The greedy policy that results from VI
	 */
	protected Policy				modelPolicy;


	/**
	 * The last initial state of an episode
	 */
	protected State					initialState;

	/**
	 * Initializes
	 * @param domain model domain
	 * @param rf model reward funciton
	 * @param tf model termination function
	 * @param gamma discount factor
	 * @param hashingFactory the hashing factory
	 * @param maxDelta max value function delta in VI
	 * @param maxIterations max iterations of VI
	 */
	public VIModelLearningPlanner(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, HashableStateFactory hashingFactory, double maxDelta, int maxIterations){
		super(domain, rf, tf, gamma, hashingFactory, maxDelta, maxIterations);
		this.modelPolicy = new ReplanIfUnseenPolicy(new GreedyQPolicy(this));
		this.toggleDebugPrinting(false);
	}
	
	
	@Override
	public void initializePlannerIn(State s) {
		this.initialState = s;
		this.observedStates.add(this.hashingFactory.hashState(s));
	}

	@Override
	public void modelChanged(State changedState) {
		this.observedStates.add(this.hashingFactory.hashState(changedState));
		this.rerunVI();
	}

	@Override
	public Policy modelPlannedPolicy() {
		return modelPolicy;
	}



	/**
	 * Reruns VI on the new updated model. It will force VI to consider all states the agent has ever previously observed, even though not all
	 * may be connected by the current unknown transition model.
	 */
	protected void rerunVI(){
		
		this.resetSolver();
		//seed state space from what we know exists
		for(HashableState sh : this.observedStates){
			this.performReachabilityFrom(sh.s);
		}
		
		//run vi
		this.runVI();
		
	}
	
	
	/**
	 * A policy that causes planning to performed if the state is unknown
	 * @author James MacGlashan
	 *
	 */
	class ReplanIfUnseenPolicy extends Policy{

		/**
		 * The source policy to follow for known states
		 */
		Policy p;
		
		
		/**
		 * Initializes with a given source policy
		 * @param p the source policy
		 */
		public ReplanIfUnseenPolicy(Policy p){
			this.p = p;
		}
		
		@Override
		public AbstractGroundedAction getAction(State s) {
			if(!VIModelLearningPlanner.this.hasComputedValueFor(s)){
				VIModelLearningPlanner.this.observedStates.add(VIModelLearningPlanner.this.hashingFactory.hashState(s));
				VIModelLearningPlanner.this.rerunVI();
			}
			return p.getAction(s);
		}

		@Override
		public List<ActionProb> getActionDistributionForState(State s) {
			
			if(!VIModelLearningPlanner.this.hasComputedValueFor(s)){
				VIModelLearningPlanner.this.observedStates.add(VIModelLearningPlanner.this.hashingFactory.hashState(s));
				VIModelLearningPlanner.this.rerunVI();
			}
			return p.getActionDistributionForState(s);
		}

		@Override
		public boolean isStochastic() {
			return p.isStochastic();
		}

		@Override
		public boolean isDefinedFor(State s) {
			return p.isDefinedFor(s);
		}
		
		
		
		
	}


}
