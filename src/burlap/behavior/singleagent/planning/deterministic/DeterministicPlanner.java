package burlap.behavior.singleagent.planning.deterministic;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import burlap.behavior.singleagent.MDPSolver;
import burlap.behavior.singleagent.planning.Planner;
import burlap.oomdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.oomdp.statehashing.HashableStateFactory;
import burlap.oomdp.statehashing.HashableState;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.state.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


/**
 * This class extends the OOMDPlanner to provide the interface and common mechanisms for classic deterministic forward search planners.
 * Since classic forward search planners search for a goal state, this class makes use of a StateConditionTest object to indicate goal states.
 * Although these planners do not strictly compute policies, but instead action sequences to be executed from some initial state, 
 * it also stores an internal partial policy
 * that specifies which action to take in each state that is on the path of a plan it has previously found. If the same
 * valueFunction is used multiple times from different initial states (but keep the same goal condition) it will progressively fill out the policy.
 * If the valueFunction fails to find a valid plan, it will throw a {@link PlanningFailedException} runtime exception.
 * @author James MacGlashan
 *
 */
public abstract class DeterministicPlanner extends MDPSolver implements Planner{

	/**
	 * This State condition test should return true for goal states and false for non-goal states.
	 */
	protected StateConditionTest						gc;
	
	/**
	 * Stores the action plan found by the valueFunction as a deterministic policy
	 */
	protected Map <HashableState, GroundedAction>		internalPolicy;
	
	
	
	/**
	 * Initializes the valueFunction. For some planners the reward function is not necessary, but for others that account for cost, like UFC, it should
	 * be provided. This method will automatically call the OOMDPPlanner init with the discount factor set to 1.
	 * @param domain the domain in which to plan.
	 * @param rf the reward function to use (probably should be negative to be compatible with most forward search planners)
	 * @param tf the terminal function
	 * @param gc test for goal conditions that should return true for goal states and false for non-goal states.
	 * @param hashingFactory the hashing factory to use for states.
	 */
	public void deterministicPlannerInit(Domain domain, RewardFunction rf, TerminalFunction tf, StateConditionTest gc, HashableStateFactory hashingFactory){
		
		this.solverInit(domain, rf, tf, 1., hashingFactory); //goal condition doubles as termination function for deterministic planners
		this.gc = gc;
		this.internalPolicy = new HashMap<HashableState, GroundedAction>();
	

	}
	
	@Override
	public void resetSolver(){
		this.mapToStateIndex.clear();
		this.internalPolicy.clear();
	}

	/**
	 * Returns whether the valueFunction has a plan solution from the provided state.
	 * @param s the state to test whether a plan solution currently exists.
	 * @return true if a plan solution from the given state exists; false otherwise.
	 */
	public boolean hasCachedPlanForState(State s){
		HashableState sh = this.stateHash(s);
		HashableState indexSH = mapToStateIndex.get(sh);
		
		return indexSH != null;
	}
	
	
	/**
	 * Returns the action suggested by the valueFunction for the given state. If a plan including this state
	 * has not already been computed, the valueFunction will be called from this state to find one.
	 * @param s the state for which the suggested action is to be returned.
	 * @return The suggested action for the given state.
	 */
	public GroundedAction querySelectedActionForState(State s){
		
		HashableState sh = this.stateHash(s);
		HashableState indexSH = mapToStateIndex.get(sh);
		if(indexSH == null){
			this.planFromState(s);
			return internalPolicy.get(sh); //no need to translate because if the state didn't exist then it got indexed with this state's rep
		}
		
		//otherwise it's already computed
		GroundedAction res = internalPolicy.get(sh);
		
		//do object matching from returned result to this query state and return result
		res = (GroundedAction)res.translateParameters(indexSH.s, sh.s);
		
				
		return res;
		
		
	}
	
	
	/**
	 * Encodes a solution path found by the valueFunction into this class's internal policy structure. If a state
	 * was visited more than once in the solution path, then the action used for the last occurrence of the
	 * state is used. If a null search node is passed to this method, it indicates that a plan was not
	 * found and a {@link PlanningFailedException} runtime exception is thrown.
	 * @param lastVisitedNode the last search node in the solution path, which should contain the goal state.
	 */
	protected void encodePlanIntoPolicy(SearchNode lastVisitedNode){
		
		if(lastVisitedNode == null){
			throw new PlanningFailedException();
		}
		
		SearchNode curNode = lastVisitedNode;
		while(curNode.backPointer != null){
			HashableState bpsh = curNode.backPointer.s;
			if(!mapToStateIndex.containsKey(bpsh)){ //makes sure earlier plan duplicate nodes do not replace the correct later visits
				internalPolicy.put(bpsh, curNode.generatingAction);
				mapToStateIndex.put(bpsh, bpsh);
			}
			
			curNode = curNode.backPointer;
		}
	}
	
	
	/**
	 * Returns true if a solution path uses an option in its solution.
	 * @param lastVisitedNode the last search node in the solution path, which should contain the goal state.
	 * @return true if a solution path uses an option in its solution; false otherwise.
	 */
	protected boolean planContainsOption(SearchNode lastVisitedNode){
		
		if(lastVisitedNode == null){
			return false;
		}
		
		SearchNode curNode = lastVisitedNode;
		while(curNode.backPointer != null){
			
			if(!curNode.generatingAction.action.isPrimitive()){
				return true;
			}
			
			curNode = curNode.backPointer;
		}
		return false;
	}
	
	
	/**
	 * Returns true if a solution path visits the same state multiple times.
	 * @param lastVisitedNode the last search node in the solution path, which should contain the goal state.
	 * @return true if a solution path uses an option in its solution; false otherwise.
	 */
	protected boolean planHasDupilicateStates(SearchNode lastVisitedNode){
		
		Set<HashableState> statesInPlan = new HashSet<HashableState>();
		SearchNode curNode = lastVisitedNode;
		while(curNode.backPointer != null){
			if(statesInPlan.contains(curNode.s)){
				return true;
			}
			statesInPlan.add(curNode.s);
			curNode = curNode.backPointer;
		}
		return false;
		
	}
	
	
	
	/**
	 * Exception class for indicating that a solution failed to be found by the planning algorithm.
	 * @author James MacGlashan
	 *
	 */
	public class PlanningFailedException extends RuntimeException{

		private static final long serialVersionUID = 1L;
		
		public PlanningFailedException(){
			super("Planning failed to find the goal state");
		}
		
		
	}
	
	
	
}
