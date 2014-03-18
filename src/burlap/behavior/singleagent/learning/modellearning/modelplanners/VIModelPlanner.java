package burlap.behavior.singleagent.learning.modellearning.modelplanners;

import java.util.List;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.learning.modellearning.ModelPlanner;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.debugtools.DPrint;
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
public class VIModelPlanner implements ModelPlanner {

	
	/**
	 * The value iteration planning object
	 */
	protected ValueIteration		vi;
	
	/**
	 * The greedy policy that results from VI
	 */
	protected Policy				modelPolicy;
	
	
	/**
	 * The last initial state of an episode
	 */
	protected State					initialState;
	
	/**
	 * the model domain
	 */
	protected Domain				domain;
	
	/**
	 * The model reward function
	 */
	protected RewardFunction		rf;
	
	/**
	 * The model termination function
	 */
	protected TerminalFunction		tf;
	
	/**
	 * The model planning discount factor
	 */
	protected double				gamma;
	
	/**
	 * The hashing factory to use
	 */
	protected StateHashFactory		hashingFactory;
	
	/**
	 * The maximium VI delta
	 */
	protected double				maxDelta;
	
	/**
	 * The maximum number of VI iterations
	 */
	protected int					maxIterations;
	
	
	
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
	public VIModelPlanner(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, StateHashFactory hashingFactory, double maxDelta, int maxIterations){
		
		this.domain = domain;
		this.rf = rf;
		this.tf = tf;
		this.gamma = gamma;
		this.hashingFactory = hashingFactory;
		this.maxDelta = maxDelta;
		this.maxIterations = maxIterations;
		
		vi = new ValueIteration(domain, rf, tf, gamma, hashingFactory, maxDelta, maxIterations);
		DPrint.toggleCode(vi.getDebugCode(), false);
		this.modelPolicy = new ReplanIfUnseenPolicy(new GreedyQPolicy(vi));
	}
	
	
	@Override
	public void initializePlannerIn(State s) {
		this.initialState = s;
	}

	@Override
	public void modelChanged(State changedState) {
		
		//makes an new instance of vi
		vi = new ValueIteration(domain, rf, tf, gamma, hashingFactory, maxDelta, maxIterations);
		this.modelPolicy = new ReplanIfUnseenPolicy(new GreedyQPolicy(vi));
		
		this.vi.planFromState(initialState);
		this.vi.planFromState(changedState);
	}

	@Override
	public Policy modelPlannedPolicy() {
		return modelPolicy;
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
			if(!VIModelPlanner.this.vi.hasComputedValueFor(s)){
				VIModelPlanner.this.vi.planFromState(s);
			}
			return p.getAction(s);
		}

		@Override
		public List<ActionProb> getActionDistributionForState(State s) {
			
			if(!VIModelPlanner.this.vi.hasComputedValueFor(s)){
				VIModelPlanner.this.vi.planFromState(s);
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
