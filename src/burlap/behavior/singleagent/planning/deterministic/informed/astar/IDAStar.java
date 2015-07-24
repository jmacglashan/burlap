package burlap.behavior.singleagent.planning.deterministic.informed.astar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import burlap.oomdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.behavior.singleagent.planning.deterministic.DeterministicPlanner;
import burlap.behavior.singleagent.planning.deterministic.SearchNode;
import burlap.behavior.singleagent.planning.deterministic.informed.Heuristic;
import burlap.behavior.singleagent.planning.deterministic.informed.PrioritizedSearchNode;
import burlap.behavior.singleagent.planning.deterministic.informed.PrioritizedSearchNode.PSNComparator;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.debugtools.DPrint;
import burlap.oomdp.auxiliary.common.NullTermination;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


/**
 * Iteratively deepening A* implementation.
 * 
 * <p/>
 * If a terminal function is provided via the setter method defined for OO-MDPs, then the BestFirst search algorithm will not expand any nodes
 * that are terminal states, as if there were no actions that could be executed from that state. Note that terminal states
 * are not necessarily the same as goal states, since there could be a fail condition from which the agent cannot act, but
 * that is not explicitly represented in the transition dynamics.
 * @author James MacGlashan
 *
 */
public class IDAStar extends DeterministicPlanner {

	/**
	 * The heuristic to use
	 */
	protected Heuristic									heuristic;
	
	/**
	 * The comparator to use for checking which nodes to expand first.
	 */
	protected PSNComparator								nodeComparator;
	
	
	/**
	 * Initializes the planner.
	 * @param domain the domain in which to plan
	 * @param rf the reward function that represents costs as negative reward
	 * @param gc should evaluate to true for goal states; false otherwise
	 * @param hashingFactory the state hashing factory to use
	 * @param heuristic the planning heuristic. Should return non-positive values.
	 */
	public IDAStar(Domain domain, RewardFunction rf, StateConditionTest gc, StateHashFactory hashingFactory, Heuristic heuristic){
		
		this.deterministicPlannerInit(domain, rf, new NullTermination(), gc, hashingFactory);
		
		this.heuristic = heuristic;
		nodeComparator = new PrioritizedSearchNode.PSNComparator();
		
	}
	
	
	
	
	@Override
	public void planFromState(State initialState) {
		
		StateHashTuple sih = this.stateHash(initialState);
		
		if(mapToStateIndex.containsKey(sih)){
			return ; //no need to plan since this is already solved
		}
		
		
		PrioritizedSearchNode initialPSN = new PrioritizedSearchNode(sih, heuristic.h(initialState));
		double nextMinR = initialPSN.priority;
		
		
		PrioritizedSearchNode solutionNode = null;
		while(solutionNode == null){
			
			PrioritizedSearchNode cand = this.FLimtedDFS(initialPSN, nextMinR, 0.);
			if(cand == null){
				return ; //FAIL CONDITION, EVERY PATH LEADS TO A DEAD END
			}
			
			//was the goal found within the limit?
			if(this.planEndNode(cand) && cand.priority >= nextMinR){
				solutionNode = cand;
			}
			nextMinR = cand.priority;
			
			if(solutionNode == null){
				DPrint.cl(debugCode, "Increase depth to F: " + nextMinR);
			}
			
		}
		
		
		//search to goal complete now follow back pointers to set policy
		this.encodePlanIntoPolicy(solutionNode);
		
		

	}
	
	
	/**
	 * Recursive method to perform A* up to a f-score depth
	 * @param lastNode the node to expand
	 * @param minR the minimum cumulative reward at which to stop the search (in other terms the maximum cost)
	 * @param cumulatedReward the amount of reward accumulated at this node
	 * @return a search node with the goal state, or null if there is no path within the reward requirements from this node
	 */
	protected PrioritizedSearchNode FLimtedDFS(PrioritizedSearchNode lastNode, double minR, double cumulatedReward){
		
		if(lastNode.priority < minR){
			return lastNode; //fail condition (either way return the last point to which you got)
		}
		if(this.planEndNode(lastNode)){
			return lastNode; //succeed condition
		}
		if(this.tf.isTerminal(lastNode.s.s)){
			return null; //treat like a dead end if we're at a terminal state
		}
		
		
		State s = lastNode.s.s;
		
		//get all actions
		/*List <GroundedAction> gas = new ArrayList<GroundedAction>();
		for(Action a : actions){
			gas.addAll(s.getAllGroundedActionsFor(a));
		}*/
		List<GroundedAction> gas = Action.getAllApplicableGroundedActionsFromActionList(this.actions, s);
		
		//generate successor nodes
		List <PrioritizedSearchNode> successors = new ArrayList<PrioritizedSearchNode>(gas.size());
		List <Double> successorGs = new ArrayList<Double>(gas.size());
		for(GroundedAction ga : gas){
			
			State ns = ga.executeIn(s);
			StateHashTuple nsh = this.stateHash(ns);
			
			double r = rf.reward(s, ga, ns);
			double g = cumulatedReward + r;
			double hr = heuristic.h(ns);
			double f = g + hr;
			PrioritizedSearchNode pnsn = new PrioritizedSearchNode(nsh, ga, lastNode, f);
			
			//only add if this does not exist on our path already
			if(this.lastStateOnPathIsNew(pnsn)){
				successors.add(pnsn);
				successorGs.add(g);
			}
			
		}
		
		//sort the successors by f-score to travel the most promising ones first
		Collections.sort(successors, nodeComparator);
		
		
		
		double maxCandR = Double.NEGATIVE_INFINITY;
		PrioritizedSearchNode bestCand = null;
		//note that since we want to expand largest expected rewards first, we should go reverse order of the f-ordered successors
		for(int i = successors.size()-1; i >= 0; i--){
			PrioritizedSearchNode snode = successors.get(i);
			PrioritizedSearchNode cand = this.FLimtedDFS(snode, minR, successorGs.get(i));
			if(cand != null){
				if(cand.priority > maxCandR){
					bestCand = cand;
					maxCandR = cand.priority;
				}
			}
			
		}
		
		return bestCand; 
	}

	
	
	
	/**
	 * Returns true if the search node wraps a goal state.
	 * @param node the node to check
	 * @return true if the search node wraps a goal state; false otherwise.
	 */
	protected boolean planEndNode(SearchNode node){
		
		if(gc.satisfies(node.s.s)){
			return true;
		}
		
		return false;
		
	}
	
	
	
	/**
	 * Returns true if the search node has not be visited previously on the current search path.
	 * @param psn the search node to check.
	 * @return true if the search node has not be visited previously on the current search path; false otherwise.
	 */
	protected boolean lastStateOnPathIsNew(PrioritizedSearchNode psn){
		
		PrioritizedSearchNode cmpNode = (PrioritizedSearchNode)psn.backPointer;
		
		while(cmpNode != null){
			if(psn.equals(cmpNode)){
				return false;
			}
			cmpNode = (PrioritizedSearchNode)cmpNode.backPointer;
		}
		
		return true;
	}
	
	
}
