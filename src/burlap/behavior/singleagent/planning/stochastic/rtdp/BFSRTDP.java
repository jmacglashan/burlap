package burlap.behavior.singleagent.planning.stochastic.rtdp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.singleagent.planning.stochastic.ActionTransitions;
import burlap.behavior.singleagent.planning.stochastic.HashedTransitionProbability;
import burlap.oomdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.behavior.statehashing.HashableStateFactory;
import burlap.behavior.statehashing.HashableState;
import burlap.debugtools.DPrint;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.states.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.RewardFunction;


/**
 * A modified version of Real-time Dynamic Programming [1] in which first a breadth-first search-like pass is made to seed the value function, and then
 * planning continues in the typical RTDP rollout-like fashion. The BFS pass either extends to all reachable states from the source state, or optionally,
 * to the depth required to visit a goal state. This approach may be useful if the depth of the optimal policy is expected to be much shorter than the depth of the entire
 * state space and if a good initial value initialization is not able to be provided. The BFS-like pass expands all possible stochastic transitions from an action.
 * 
 * 
 * 1. Barto, Andrew G., Steven J. Bradtke, and Satinder P. Singh. "Learning to act using real-time dynamic programming." Artificial Intelligence 72.1 (1995): 81-138.
 * 
 * @author James MacGlashan
 *
 */
public class BFSRTDP extends RTDP {

	/**
	 * indicates whether the BFS-like pass has already been performed.
	 */
	protected boolean												performedInitialPlan;
	
	/**
	 * The goal condition that stops the BFS-like pass
	 */
	protected StateConditionTest									goalCondition;
	
	
	/**
	 * Initializes the valueFunction. The value function will be initialized to vInit by default everywhere and will use a greedy policy with random tie breaks
	 * for performing rollouts. Use the {@link #setValueFunctionInitialization(ValueFunctionInitialization)} method
	 * to change the value function initialization and the {@link #setRollOutPolicy(Policy)} method to change the rollout policy to something else.
	 * @param domain the domain in which to plan
	 * @param rf the reward function
	 * @param tf the terminal state function
	 * @param gamma the discount factor
	 * @param hashingFactory the state hashing factor to use
	 * @param vInit the value to the the value function for all states will be initialized
	 * @param numRollouts the number of rollouts to perform when planning is started.
	 * @param maxDelta when the maximum change in the value function from a rollout is smaller than this value, planning will terminate.
	 * @param maxDepth the maximum depth/length of a rollout before it is terminated and Bellman updates are performed.
	 */
	public BFSRTDP(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, HashableStateFactory hashingFactory, double vInit, int numRollouts, double maxDelta, int maxDepth){
		
		super(domain, rf, tf, gamma, hashingFactory, vInit, numRollouts, maxDelta, maxDepth);

		this.performedInitialPlan = false;
		this.goalCondition = null;

	}
	
	
	
	/**
	 * Initializes the valueFunction. The value function will be initialized to vInit by default everywhere and will use a greedy policy with random tie breaks
	 * for performing rollouts. Use the {@link #setValueFunctionInitialization(ValueFunctionInitialization)} method
	 * to change the value function initialization and the {@link #setRollOutPolicy(Policy)} method to change the rollout policy to something else.
	 * @param domain the domain in which to plan
	 * @param rf the reward function
	 * @param tf the terminal state function
	 * @param gamma the discount factor
	 * @param hashingFactory the state hashing factor to use
	 * @param vInit the value to the the value function for all states will be initialized
	 * @param numRollouts the number of rollouts to perform when planning is started.
	 * @param maxDelta when the maximum change in the value function from a rollout is smaller than this value, VI will terminate.
	 * @param maxDepth the maximum depth/length of a rollout before it is terminated and Bellman updates are performed.
	 * @param goalCondition a state condition test that returns true for goal states. Causes the BFS-like pass to stop expanding when found.
	 */
	public BFSRTDP(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, HashableStateFactory hashingFactory, double vInit, int numRollouts, double maxDelta, int maxDepth, StateConditionTest goalCondition){
		
		super(domain, rf, tf, gamma, hashingFactory, vInit, numRollouts, maxDelta, maxDepth);

		this.performedInitialPlan = false;
		this.goalCondition = goalCondition;

	}
	

	/**
	 * Sets the goal state that causes the BFS-like pass to stop expanding when found.
	 * @param gc
	 */
	public void setGoalCondition(StateConditionTest gc){
		this.goalCondition = gc;
	}


	/**
	 * Plans from the input state and then returns a {@link burlap.behavior.policy.GreedyQPolicy} that greedily
	 * selects the action with the highest Q-value and breaks ties uniformly randomly.
	 * @param initialState the initial state of the planning problem
	 * @return a {@link burlap.behavior.policy.GreedyQPolicy}.
	 */
	@Override
	public GreedyQPolicy planFromState(State initialState) {
		HashableState sh = this.stateHash(initialState);
		if(!mapToStateIndex.containsKey(sh)){
			this.performInitialPassFromState(initialState);
		}
		return super.planFromState(initialState);

	}
	
	
	/**
	 * Performs a BFS-like pass to either all reachable states or to depth at which a goal state is found and then performs the Bellman update on all those states.
	 * @param initialState the initial state from which to perform the BFS-like pass.
	 */
	protected void performInitialPassFromState(State initialState){
		
		List <HashableState> orderedStates = this.performRecahabilityAnalysisFrom(initialState);
		this.performOrderedBellmanUpdates(orderedStates);
		
		performedInitialPlan = true;
		
	}
	
	
	
	
	
	/**
	 * Finds either all reachable states from si or all states up to the depth that the first goal state is found from si.
	 * @param si the initial state from which to search for states
	 * @return the list of all states found
	 */
	protected List <HashableState> performRecahabilityAnalysisFrom(State si){
		
		DPrint.cl(debugCode, "Starting reachability analysis");
		
		HashableState sih = this.stateHash(si);
		//first check if this is an new state, otherwise we do not need to do any new reachability analysis
		if(transitionDynamics.containsKey(sih)){
			return new ArrayList<HashableState>(); //no need for additional reachability testing so return empty closed list
		}
		
		//add to the open list
		LinkedList <HashableState> closedList = new LinkedList<HashableState>();
		LinkedList <HashableState> openList = new LinkedList<HashableState>();
		Set <HashableState> openedSet = new HashSet<HashableState>();
		openList.offer(sih);
		openedSet.add(sih);
		
		while(openList.size() > 0){
			HashableState sh = openList.poll();
			
			//skip this if it's already been expanded
			if(transitionDynamics.containsKey(sh)){
				continue;
			}
			
			//otherwise do expansion
			//if we reached a goal state then then BFS completes and we do not need to add terminal's children
			if(this.satisfiesGoal(sh.s)){
				break;
			}
			
			if(this.tf.isTerminal(sh.s)){
				continue; //cannot act from here
			}
			
			
			//get the transition dynamics for each action and queue up new states
			List <ActionTransitions> transitions = this.getActionsTransitions(sh);
			for(ActionTransitions at : transitions){
				for(HashedTransitionProbability tp : at.transitions){
					HashableState tsh = tp.sh;
					if(!openedSet.contains(tsh) && !transitionDynamics.containsKey(tsh)){
						openedSet.add(tsh);
						openList.offer(tsh);
					}
				}
				
			}

			//close it
			closedList.addFirst(sh);
			
		}
		
		
		DPrint.cl(debugCode, "Finished reachability analysis; # states: " + transitionDynamics.size());
		
		
		return closedList;
	}
	
	
	/**
	 * Returns whether a state is a goal state.
	 * @param s the state to test.
	 * @return true if s is a goal state; false otherwise.
	 */
	protected boolean satisfiesGoal(State s){
		if(goalCondition == null){
			return false;
		}
		return goalCondition.satisfies(s);
	}
	

}
