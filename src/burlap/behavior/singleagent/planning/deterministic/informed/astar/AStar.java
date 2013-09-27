package burlap.behavior.singleagent.planning.deterministic.informed.astar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.deterministic.informed.BestFirst;
import burlap.behavior.singleagent.planning.deterministic.informed.Heuristic;
import burlap.behavior.singleagent.planning.deterministic.informed.PrioritizedSearchNode;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.datastructures.HashIndexedHeap;
import burlap.oomdp.auxiliary.common.NullTermination;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


public class AStar extends BestFirst{

	
	protected Heuristic									heuristic;
	protected Map <StateHashTuple, Double> 				cumulatedRewardMap;
	protected double									lastComputedCumR;
	
	public AStar(Domain domain, RewardFunction rf, StateConditionTest gc, StateHashFactory hashingFactory, Heuristic heuristic){
		
		this.deterministicPlannerInit(domain, rf, new NullTermination(), gc, hashingFactory);
		
		this.heuristic = heuristic;
		
	}

	


	@Override
	public void prePlanPrep(){
		cumulatedRewardMap = new HashMap<StateHashTuple, Double>();
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
	public double computeF(PrioritizedSearchNode parentNode, GroundedAction generatingAction, StateHashTuple successorState) {
		double cumR = 0.;
		double r = 0.;
		if(parentNode != null){
			double pCumR = cumulatedRewardMap.get(parentNode.s);
			r = rf.reward(parentNode.s.s, generatingAction, successorState.s);
			cumR = pCumR + r;
		}
		
		double H  = heuristic.h(successorState.s);
		lastComputedCumR = cumR;
		double F = cumR + H;
		
		return F;
	}

	
	
	

	
	
	
	
	
	
}
