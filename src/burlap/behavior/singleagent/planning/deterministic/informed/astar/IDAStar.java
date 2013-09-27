package burlap.behavior.singleagent.planning.deterministic.informed.astar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.deterministic.DeterministicPlanner;
import burlap.behavior.singleagent.planning.deterministic.SearchNode;
import burlap.behavior.singleagent.planning.deterministic.informed.Heuristic;
import burlap.behavior.singleagent.planning.deterministic.informed.PrioritizedSearchNode;
import burlap.behavior.singleagent.planning.deterministic.informed.PrioritizedSearchNode.PSNComparator;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.debugtools.DPrint;
import burlap.oomdp.auxiliary.common.NullTermination;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


public class IDAStar extends DeterministicPlanner {

	
	protected Heuristic									heuristic;
	protected PSNComparator								nodeComparator;
	
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
		//double nextMinR = -97;
		
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
	
	protected PrioritizedSearchNode FLimtedDFS(PrioritizedSearchNode lastNode, double minR, double cumulatedReward){
		
		if(lastNode.priority < minR){
			return lastNode; //fail condition (either way return the last point to which you got)
		}
		if(this.planEndNode(lastNode)){
			return lastNode; //succeed condition
		}
		
		
		State s = lastNode.s.s;
		
		//get all actions
		List <GroundedAction> gas = new ArrayList<GroundedAction>();
		for(Action a : actions){
			gas.addAll(s.getAllGroundedActionsFor(a));
		}
		
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

	
	
	
	
	protected boolean planEndNode(SearchNode node){
		
		if(gc.satisfies(node.s.s)){
			return true;
		}
		
		return false;
		
	}
	
	
	
	
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
