package burlap.behavior.singleagent.planning.stochastic.policyiteration;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.planning.stochastic.ActionTransitions;
import burlap.behavior.singleagent.planning.stochastic.HashedTransitionProbability;
import burlap.behavior.policy.SolverDerivedPolicy;
import burlap.behavior.singleagent.planning.stochastic.DynamicProgramming;
import burlap.behavior.policy.GreedyDeterministicQPolicy;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.debugtools.DPrint;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.states.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.RewardFunction;

public class PolicyIteration extends DynamicProgramming implements Planner {

	/**
	 * When the maximum change in the value function is smaller than this value, policy evaluation will terminate. 
	 */
	protected double												maxEvalDelta;
	
	
	/**
	 * When the maximum change between policy evaluations is smaller than this value, planning will terminate.
	 */
	protected double												maxPIDelta;
	
	/**
	 * When the number of policy evaluation iterations exceeds this value, policy evaluation will terminate.
	 */
	protected int													maxIterations;
	
	
	/**
	 * When the number of policy iterations passes this value, planning will terminate.
	 */
	protected int													maxPolicyIterations;
	
	/**
	 * The current policy to be evaluated
	 */
	protected SolverDerivedPolicy evaluativePolicy;
	
	
	/**
	 * Indicates whether the reachable states has been computed yet.
	 */
	protected boolean												foundReachableStates = false;
	
	
	
	/**
	 * Initializes the planner.
	 * @param domain the domain in which to plan
	 * @param rf the reward function
	 * @param tf the terminal state function
	 * @param gamma the discount factor
	 * @param hashingFactory the state hashing factor to use
	 * @param maxDelta when the maximum change in the value function is smaller than this value, policy evaluation will terminate. Similarly, when the maximum value value function change between policy iterations is smaller than this value planning will terminate.
	 * @param maxEvaluationIterations when the number of policy evaluation iterations exceeds this value, policy evaluation will terminate.
	 * @param maxPolicyIterations when the number of policy iterations passes this value, planning will terminate.
	 */
	public PolicyIteration(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, StateHashFactory hashingFactory, double maxDelta, int maxEvaluationIterations, int maxPolicyIterations){
		this.DPPInit(domain, rf, tf, gamma, hashingFactory);
		
		this.maxEvalDelta = maxDelta;
		this.maxPIDelta = maxDelta;
		this.maxIterations = maxEvaluationIterations;
		this.maxPolicyIterations = maxPolicyIterations;
		
		this.evaluativePolicy = new GreedyDeterministicQPolicy(this);
	}
	
	
	/**
	 * Initializes the planner.
	 * @param domain the domain in which to plan
	 * @param rf the reward function
	 * @param tf the terminal state function
	 * @param gamma the discount factor
	 * @param hashingFactory the state hashing factor to use
	 * @param maxPIDelta when the maximum value value function change between policy iterations is smaller than this value planning will terminate.
	 * @param maxEvalDelta when the maximum change in the value function is smaller than this value, policy evaluation will terminate.
	 * @param maxEvaluationIterations when the number of policy evaluation iterations exceeds this value, policy evaluation will terminate.
	 * @param maxPolicyIterations when the number of policy iterations passes this value, planning will terminate.
	 */
	public PolicyIteration(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, StateHashFactory hashingFactory, double maxPIDelta, double maxEvalDelta, int maxEvaluationIterations, int maxPolicyIterations){
		this.DPPInit(domain, rf, tf, gamma, hashingFactory);
		
		this.maxEvalDelta = maxEvalDelta;
		this.maxPIDelta = maxPIDelta;
		this.maxIterations = maxEvaluationIterations;
		this.maxPolicyIterations = maxPolicyIterations;
		
		this.evaluativePolicy = new GreedyDeterministicQPolicy(this);
	}
	
	
	/**
	 * Sets which kind of policy to use whenever the policy is updated. The default is a deterministic greedy policy ({@link burlap.behavior.policy.GreedyDeterministicQPolicy}.
	 * @param p the policy to use when updating to the new evaluated value function.
	 */
	public void setPolicyClassToEvaluate(SolverDerivedPolicy p){
		this.evaluativePolicy = p;
	}
	
	
	/**
	 * Returns the policy that was last computed.
	 * @return the policy that was last computed.
	 */
	public Policy getComputedPolicy(){
		return (Policy)this.evaluativePolicy;
	}
	
	/**
	 * Calling this method will force the planner to recompute the reachable states when the {@link #planFromState(State)} method is called next.
	 * This may be useful if the transition dynamics from the last planning call have changed and if planning needs to be restarted as a result.
	 */
	public void recomputeReachableStates(){
		this.foundReachableStates = false;
	}
	
	
	
	
	@Override
	public void planFromState(State initialState) {

		int iterations = 0;
		this.initializeOptionsForExpectationComputations();
		if(this.performReachabilityFrom(initialState)){
			
			double delta;
			do{
				DynamicProgramming lastValueFunction = this.getCopyOfValueFunction();
				this.evaluativePolicy.setSolver(lastValueFunction);
				delta = this.evaluatePolicy();
				iterations++;
			}while(delta > this.maxPIDelta && iterations < maxPolicyIterations);
			
		}
		

	}
	
	
	@Override
	public void resetSolver(){
		super.resetSolver();
		this.foundReachableStates = false;
	}
	
	/**
	 * Computes the value function under following the current evaluative policy.
	 * @return the maximum single iteration change in the value function
	 */
	protected double evaluatePolicy(){
		
		if(!this.foundReachableStates){
			throw new RuntimeException("Cannot run VI until the reachable states have been found. Use planFromState method at least once or instead.");
		}
		
		double maxChangeInPolicyEvaluation = Double.NEGATIVE_INFINITY;
		
		Set <StateHashTuple> states = mapToStateIndex.keySet();
		
		int i = 0;
		for(i = 0; i < this.maxIterations; i++){
			
			double delta = 0.;
			for(StateHashTuple sh : states){
				
				double v = this.value(sh);
				double maxQ = this.performFixedPolicyBellmanUpdateOn(sh, (Policy)this.evaluativePolicy);
				delta = Math.max(Math.abs(maxQ - v), delta);
				
			}
			
			maxChangeInPolicyEvaluation = Math.max(delta, maxChangeInPolicyEvaluation);
			
			if(delta < this.maxEvalDelta){
				break; //approximated well enough; stop iterating
			}
			
		}
		
		DPrint.cl(this.debugCode, "Policy Eval Passes: " + i);
		
		return maxChangeInPolicyEvaluation;
		
	}
	
	
	
	
	
	/**
	 * This method will find all reachable states that will be used when computing the value function.
	 * This method will not do anything if all reachable states from the input state have been discovered from previous calls to this method.
	 * @param si the source state from which all reachable states will be found
	 * @return true if a reachability analysis had never been performed from this state; false otherwise.
	 */
	public boolean performReachabilityFrom(State si){
		
		
		
		StateHashTuple sih = this.stateHash(si);
		//if this is not a new state and we are not required to perform a new reachability analysis, then this method does not need to do anything.
		if(transitionDynamics.containsKey(sih) && this.foundReachableStates){
			return false; //no need for additional reachability testing
		}
		
		DPrint.cl(this.debugCode, "Starting reachability analysis");
		
		//add to the open list
		LinkedList <StateHashTuple> openList = new LinkedList<StateHashTuple>();
		Set <StateHashTuple> openedSet = new HashSet<StateHashTuple>();
		openList.offer(sih);
		openedSet.add(sih);
		
		
		while(openList.size() > 0){
			StateHashTuple sh = openList.poll();
			
			//skip this if it's already been expanded
			if(transitionDynamics.containsKey(sh)){
				continue;
			}
			
			mapToStateIndex.put(sh, sh);
			
			//do not need to expand from terminal states
			if(this.tf.isTerminal(sh.s)){
				continue;
			}
			
			
			//get the transition dynamics for each action and queue up new states
			List <ActionTransitions> transitions = this.getActionsTransitions(sh);
			for(ActionTransitions at : transitions){
				for(HashedTransitionProbability tp : at.transitions){
					StateHashTuple tsh = tp.sh;
					if(!openedSet.contains(tsh) && !transitionDynamics.containsKey(tsh)){
						openedSet.add(tsh);
						openList.offer(tsh);
					}
				}
				
			}
			
			
		}
		
		DPrint.cl(this.debugCode, "Finished reachability analysis; # states: " + mapToStateIndex.size());
		
		this.foundReachableStates = true;
		
		return true;
		
	}
	
	
	
	

	
	
}
