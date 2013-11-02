package burlap.behavior.singleagent.planning.stochastic.rtdp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.planning.ActionTransitions;
import burlap.behavior.singleagent.planning.HashedTransitionProbability;
import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.debugtools.DPrint;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


/**
 * A modified version of Real-time Dynamic Programming [1] in which first a breadth-first search-like pass is made to see the value function, and then
 * planning continues in the typical RTDP rollout-like fashion. The BFS pass either extends to all reachable states from the source state, or optionally,
 * to the depth required to visit a goal state. This approach may be useful if the depth of the optimal policy is expected to be much shorter than the depth of the entire
 * state space. The BFS-like pass expands all possible stochastic transitions from an action.
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
	 * Initializes the planner.
	 * @param domain the domain in which to plan
	 * @param rf the reward function
	 * @param tf the terminal state function
	 * @param gamma the discount factor
	 * @param hashingFactory the state hashing factor to use
	 * @param numRollouts the number of rollouts to perform when planning is started.
	 * @param maxDelta when the maximum change in the value function from a rollout is smaller than this value, VI will terminate.
	 * @param maxDepth the maximum depth/length of a rollout before it is terminated and Bellman updates are performed.
	 */
	public BFSRTDP(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, StateHashFactory hashingFactory, int numRollouts, double maxDelta, int maxDepth){
		
		super(domain, rf, tf, gamma, hashingFactory, numRollouts, maxDelta, maxDepth);

		this.performedInitialPlan = false;
		this.goalCondition = null;

	}
	
	
	
	/**
	 * Initializes the planner.
	 * @param domain the domain in which to plan
	 * @param rf the reward function
	 * @param tf the terminal state function
	 * @param gamma the discount factor
	 * @param hashingFactory the state hashing factor to use
	 * @param numRollouts the number of rollouts to perform when planning is started.
	 * @param maxDelta when the maximum change in the value function from a rollout is smaller than this value, VI will terminate.
	 * @param maxDepth the maximum depth/length of a rollout before it is terminated and Bellman updates are performed.
	 * @param goalCondition a state condition test that returns true for goal states. Causes the BFS-like pass to stop expanding when found.
	 */
	public BFSRTDP(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, StateHashFactory hashingFactory, int numRollouts, double maxDelta, int maxDepth, StateConditionTest goalCondition){
		
		super(domain, rf, tf, gamma, hashingFactory, numRollouts, maxDelta, maxDepth);

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
	
	
	@Override
	public void planFromState(State initialState) {
		StateHashTuple sh = this.stateHash(initialState);
		if(!mapToStateIndex.containsKey(sh)){
			this.performInitialPassFromState(initialState);
		}
		else{
			this.performRolloutPassesFromState(initialState);
		}

	}
	
	
	/**
	 * Performs a BFS-like pass to either all reachable states or to depth at which a goal state is found and then performs the Bellman update on all those states.
	 * @param initialState the initial state from which to perform the BFS-like pass.
	 */
	protected void performInitialPassFromState(State initialState){
		
		List <StateHashTuple> orderedStates = this.performRecahabilityAnalysisFrom(initialState);
		this.performOrderedBellmanUpdates(orderedStates);
		
		performedInitialPlan = true;
		
	}
	
	/**
	 * Performs the standard RTDP rollout and Bellman updates.
	 * @param initialState the initial state from which to the perform the rollouts.
	 */
	protected void performRolloutPassesFromState(State initialState){
		
		for(int i = 0; i < this.numRollouts; i++){
			
			EpisodeAnalysis ea = this.rollOutPolicy.evaluateBehavior(initialState, rf, tf, maxDepth);
			LinkedList <StateHashTuple> orderedStates = new LinkedList<StateHashTuple>();
			for(State s : ea.stateSequence){
				orderedStates.addFirst(this.stateHash(s));
			}
			
			double delta = this.performOrderedBellmanUpdates(orderedStates);
			if(delta < this.maxDelta){
				break;
			}
		}
		
		
	}
	
	
	
	
	/**
	 * Finds either all reachable states from si or all states up to the depth that the first goal state is found from si.
	 * @param si the initial state from which to search for states
	 * @return the list of all states found
	 */
	protected List <StateHashTuple> performRecahabilityAnalysisFrom(State si){
		
		DPrint.cl(debugCode, "Starting reachability analysis");
		
		StateHashTuple sih = this.stateHash(si);
		//first check if this is an new state, otherwise we do not need to do any new reachability analysis
		if(transitionDynamics.containsKey(sih)){
			return new ArrayList<StateHashTuple>(); //no need for additional reachability testing so return empty closed list
		}
		
		//add to the open list
		LinkedList <StateHashTuple> closedList = new LinkedList<StateHashTuple>();
		LinkedList <StateHashTuple> openList = new LinkedList<StateHashTuple>();
		Set <StateHashTuple> openedSet = new HashSet<StateHashTuple>();
		openList.offer(sih);
		openedSet.add(sih);
		
		List <Action> actions = domain.getActions();
		
		
		while(openList.size() > 0){
			StateHashTuple sh = openList.poll();
			
			//skip this if it's already been expanded
			if(transitionDynamics.containsKey(sh)){
				continue;
			}
			
			//otherwise do expansion
			//if we reached a goal state or a previously explored state, then then bfs completes and we do not need to add terminal's children
			if(this.satisfiesGoal(sh.s) || mapToStateIndex.containsKey(sh)){
				break;
			}
			
			if(this.tf.isTerminal(sh.s)){
				continue; //cannot act from here
			}
			
			
			//first get all grounded actions for this state
			List <GroundedAction> gas = new ArrayList<GroundedAction>();
			for(Action a : actions){
				gas.addAll(sh.s.getAllGroundedActionsFor(a));
			}
			
			//then get the transition dynamics for each action and queue up new states
			List <ActionTransitions> transitions = new ArrayList<ActionTransitions>();
			for(GroundedAction ga : gas){
				ActionTransitions at = new ActionTransitions(sh.s, ga, hashingFactory);
				transitions.add(at);
				for(HashedTransitionProbability tp : at.transitions){
					StateHashTuple tsh = tp.sh;
					if(!openedSet.contains(tsh) && !transitionDynamics.containsKey(tsh)){
						openedSet.add(tsh);
						openList.offer(tsh);
					}
				}
			}
			
			//now make entry for this in the transition dynamics
			transitionDynamics.put(sh, transitions);

			//close it
			closedList.addFirst(sh);
			
		}
		
		
		for(StateHashTuple sh : closedList){
			//add as visited states
			mapToStateIndex.put(sh, sh);
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
