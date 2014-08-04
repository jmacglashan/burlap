package burlap.behavior.singleagent.planning.stochastic.valueiteration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import burlap.behavior.affordances.AffordancesController;
import burlap.behavior.singleagent.planning.ActionTransitions;
import burlap.behavior.singleagent.planning.HashedTransitionProbability;
import burlap.behavior.singleagent.planning.ValueFunctionPlanner;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.debugtools.DPrint;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;




/**
 * An implementation of asynchronous value iteration. Values of states are updated using the Bellman operator in an arbitrary order and a complete pass
 * over the state space is performed on each iteration. VI can be set to terminate under two possible conditions: when the maximum change in the value
 * function is smaller than some threshold or when a threshold of iterations is passed. This implementation first determines the state space by finding
 * all reachable states from a source state. The worst case time complexity of the reachability operation is equivalent to that of one VI iteration and has the added benefit
 * that VI does not pass over non-reachable states.
 * 
 * This implementation is compatible with options.
 * 
 * 
 * @author James MacGlashan
 *
 */
public class AffordanceValueIteration extends ValueFunctionPlanner{

	/**
	 * When the maximum change in the value function is smaller than this value, VI will terminate.
	 */
	protected double												maxDelta;
	
	/**
	 * When the number of VI iterations exceeds this value, VI will terminate.
	 */
	protected int													maxIterations;
	
	
	/**
	 * Indicates whether the reachable states has been computed yet.
	 */
	protected boolean												foundReachableStates = false;
	
	
	/**
	 * When the reachability analysis to find the state space is performed, a breadth first search-like pass
	 * (spreading over all stochastic transitions) is performed. It can optionally be set so that the
	 * search is pruned at terminal states by setting this value to true. By default, it is false and the full
	 * reachable state space is found
	 */
	protected boolean												stopReachabilityFromTerminalStates = true;
	
	protected AffordancesController									affController;
	
	/**
	 * Initializers the planner.
	 * @param domain the domain in which to plan
	 * @param rf the reward function
	 * @param tf the terminal state function
	 * @param gamma the discount factor
	 * @param hashingFactory the state hashing factor to use
	 * @param maxDelta when the maximum change in the value function is smaller than this value, VI will terminate.
	 * @param maxIterations when the number of VI iterations exceeds this value, VI will terminate.
	 */
	public AffordanceValueIteration(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, StateHashFactory hashingFactory, double maxDelta, int maxIterations, AffordancesController affController){
		
		this.VFPInit(domain, rf, tf, gamma, hashingFactory);
		
		this.maxDelta = maxDelta;
		this.maxIterations = maxIterations;
		
		this.affController = affController;
	}
	
	
	/**
	 * Calling this method will force the planner to recompute the reachable states when the {@link #planFromState(State)} method is called next.
	 * This may be useful if the transition dynamics from the last planning call have changed and if planning needs to be restarted as a result.
	 */
	public void recomputeReachableStates(){
		this.foundReachableStates = false;
		this.transitionDynamics = new HashMap<StateHashTuple, List<ActionTransitions>>();
	}
	
	
	/**
	 * Sets whether the state reachability search to generate the state space will be prune the search from terminal states.
	 * The default is not to prune.
	 * @param toggle true if the search should prune the search at terminal states; false if the search should find all reachable states regardless of terminal states.
	 */
	public void toggleReachabiltiyTerminalStatePruning(boolean toggle){
		this.stopReachabilityFromTerminalStates = toggle;
	}
	
	
	@Override
	public void planFromState(State initialState){
		this.initializeOptionsForExpectationComputations();
		if(this.performAffordanceReachabilityFrom(initialState)){
			this.runAffordanceVI();
		}
			
	}
	
	public int planFromStateAndCount(State initialState){
		this.initializeOptionsForExpectationComputations();
		if(this.performAffordanceReachabilityFrom(initialState)){
			return this.runAffordanceVI();
		}
		
		return 0;
			
	}
	
	@Override
	public void resetPlannerResults(){
		super.resetPlannerResults();
		this.foundReachableStates = false;
	}
	
	/**
	 * Runs VI until the specified termination conditions are met. In general, this method should only be called indirectly through the {@link #planFromState(State)} method.
	 * The {@link #performAffordanceReachabilityFrom(State)} must have been performed at least once
	 * in the past or a runtime exception will be thrown. The {@link #planFromState(State)} method will automatically call the {@link #performAffordanceReachabilityFrom(State)} 
	 * method first and then this if it hasn't been run.
	 */
	public int runAffordanceVI(){
		
		if(!this.foundReachableStates){
			throw new RuntimeException("Cannot run VI until the reachable states have been found. Use planFromState method at least once or instead.");
		}
		
		int bellmanUpdates = 0;
		
		Set <StateHashTuple> states = mapToStateIndex.keySet();
		
		int i = 0;
		for(i = 0; i < this.maxIterations; i++){
			
			double delta = 0.;
			for(StateHashTuple sh : states){
				
				double v = this.value(sh);
//				double maxQ = this.performBellmanUpdateOn(sh.s);
				double maxQ = this.performAffordanceBellmanUpdateOn(sh, this.affController);
				bellmanUpdates++;
				delta = Math.max(Math.abs(maxQ - v), delta);
				
			}
			
			if(delta < this.maxDelta){
				break; //approximated well enough; stop iterating
			}
			
		}
		
		DPrint.cl(this.debugCode, "Passes: " + i);
		
		return bellmanUpdates;
	}
	
	
	/**
	 * This method will find all reachable states that will be used by the {@link #runAffordanceVI()} method and will cache all the transition dynamics.
	 * This method will not do anything if all reachable states from the input state have been discovered from previous calls to this method.
	 * @param si the source state from which all reachable states will be found
	 * @return true if a reachability analysis had never been performed from this state; false otherwise.
	 */
	public boolean performAffordanceReachabilityFrom(State si){
		
		StateHashTuple sih = this.stateHash(si);
		// If this is not a new state and we are not required to perform a new reachability analysis, then this method does not need to do anything.
		if(mapToStateIndex.containsKey(sih) && this.foundReachableStates){
			return false; // No need for additional reachability testing
		}
		
		DPrint.cl(this.debugCode, "Starting reachability analysis");
		
		// Add to the open list
		LinkedList <StateHashTuple> openList = new LinkedList<StateHashTuple>();
		Set <StateHashTuple> openedSet = new HashSet<StateHashTuple>();
		openList.offer(sih);
		openedSet.add(sih);
		
		
		while(openList.size() > 0){
			StateHashTuple sh = openList.poll();
			
			//skip this if it's already been expanded
			if(mapToStateIndex.containsKey(sh)){
				continue;
			}
			
			mapToStateIndex.put(sh, sh);
			
			//do not need to expand from terminal states if set to prune
			if(this.tf.isTerminal(sh.s) && stopReachabilityFromTerminalStates){
				System.out.println("(AffordanceValueIteration)reached terminal");
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
	
	/**
	 * Returns the stored action transitions for the given state. If the action transitions
	 * are not already cached and this object is set to use caching, then they will be cached.
	 * @param sh the input state from which to get the transitions
	 * @return the stored action transitions for the given state
	 */
	@Override
	protected List <ActionTransitions> getActionsTransitions(StateHashTuple sh){
		List <ActionTransitions> allTransitions = transitionDynamics.get(sh);
		
		if(allTransitions == null){
			// Need to create them
			
			// Indicate how this state is stored
			mapToStateIndex.put(sh, sh);
			
			
			// First get all grounded actions for this state
			List <AbstractGroundedAction> gas = new ArrayList<AbstractGroundedAction>();
			for(Action a : actions){
				gas.addAll(a.getAllApplicableGroundedActions(sh.s));
			}
			
			// Now filter out bad actions using affordace knowledge base
			List<AbstractGroundedAction> prunedActions = this.affController.filterIrrelevantActionsInState(gas, sh.s);

			// Now add transitions
			allTransitions = new ArrayList<ActionTransitions>(gas.size());
			for(AbstractGroundedAction ga : prunedActions){
				ActionTransitions at = new ActionTransitions(sh.s, (GroundedAction)ga, hashingFactory);
				allTransitions.add(at);
			}
			
			// Set it if we're caching
			if(this.useCachedTransitions){
				transitionDynamics.put(sh, allTransitions);
			}
			
		}
		
		return allTransitions;
	}
	
	
	

	
	
}
