package burlap.behavior.singleagent.planning.deterministic.informed.astar;

import burlap.behavior.singleagent.planning.deterministic.informed.BestFirst;
import burlap.behavior.singleagent.planning.deterministic.informed.Heuristic;
import burlap.behavior.singleagent.planning.deterministic.informed.PrioritizedSearchNode;
import burlap.datastructures.HashIndexedHeap;
import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.Action;
import burlap.mdp.singleagent.SADomain;
import burlap.mdp.statehashing.HashableState;
import burlap.mdp.statehashing.HashableStateFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of A*. The typical "costs" of A* should be represented by negative reward returned by the reward function.
 * Similarly, the heuristic function should return non-positive values and an admissible heuristic would be h(n) &gt;= C(n) for all n.
 * A* computes the f-score as g(n) + h(n) where g(n) is the cost so far to node n and h(n) is the admissible heuristic to estimate
 * the remaining cost. Again, costs should be represented as negative reward.
 * <p>
 * If a terminal function is provided via the setter method defined for OO-MDPs, then the search algorithm will not expand any nodes
 * that are terminal states, as if there were no actions that could be executed from that state. Note that terminal states
 * are not necessarily the same as goal states, since there could be a fail condition from which the agent cannot act, but
 * that is not explicitly represented in the transition dynamics.
 * 
 * @author James MacGlashan
 *
 */
public class AStar extends BestFirst{

	/**
	 * The heuristic function.
	 */
	protected Heuristic									heuristic;
	
	/**
	 * Data structure for maintaining g(n): the cost so far to node n
	 */
	protected Map <HashableState, Double> 				cumulatedRewardMap;
	
	/**
	 * Store the most recent cumulative reward received to some node.
	 */
	protected double									lastComputedCumR;
	
	
	/**
	 * Initializes A*. Goal states are indicated by gc evaluating to true. The costs are stored as negative rewards in the reward function.
	 * By default there are no terminal states except teh goal states, so a terminal function is not taken.
	 * @param domain the domain in which to plan
	 * @param gc should evaluate to true for goal states; false otherwise
	 * @param hashingFactory the state hashing factory to use
	 * @param heuristic the planning heuristic. Should return non-positive values.
	 */
	public AStar(SADomain domain, StateConditionTest gc, HashableStateFactory hashingFactory, Heuristic heuristic){
		
		this.deterministicPlannerInit(domain, gc, hashingFactory);
		
		this.heuristic = heuristic;
		
	}

	


	@Override
	public void prePlanPrep(){
		cumulatedRewardMap = new HashMap<HashableState, Double>();
	}
	
	@Override
	public void postPlanPrep(){
		cumulatedRewardMap = null; //clear to free memory
	}
	
	@Override
	public void insertIntoOpen(HashIndexedHeap<PrioritizedSearchNode> openQueue, PrioritizedSearchNode psn){
		super.insertIntoOpen(openQueue, psn);
		cumulatedRewardMap.put(psn.s, lastComputedCumR);
	}
	
	@Override
	public void updateOpen(HashIndexedHeap<PrioritizedSearchNode> openQueue, PrioritizedSearchNode openPSN, PrioritizedSearchNode npsn){
		super.updateOpen(openQueue, openPSN, npsn);
		cumulatedRewardMap.put(npsn.s, lastComputedCumR);
	}


	@Override
	public double computeF(PrioritizedSearchNode parentNode, Action generatingAction, HashableState successorState, double r) {
		double cumR = 0.;
		if(parentNode != null){
			double pCumR = cumulatedRewardMap.get(parentNode.s);
			cumR = pCumR + r;
		}
		
		double H  = heuristic.h(successorState.s);
		lastComputedCumR = cumR;
		double F = cumR + H;
		
		return F;
	}

	
	
	

	
	
	
	
	
	
}
