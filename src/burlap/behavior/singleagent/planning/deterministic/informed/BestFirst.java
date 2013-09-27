package burlap.behavior.singleagent.planning.deterministic.informed;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.behavior.singleagent.planning.deterministic.DeterministicPlanner;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.datastructures.HashIndexedHeap;
import burlap.debugtools.DPrint;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;


public abstract class BestFirst extends DeterministicPlanner {

	
	
	public abstract double computeF(PrioritizedSearchNode parentNode, GroundedAction generatingAction, StateHashTuple successorState);
	
	
	public void prePlanPrep(){
		//do nothing for default
	}
	
	public void postPlanPrep(){
		//do nothing for default
	}
	
	public void insertIntoOpen(HashIndexedHeap<PrioritizedSearchNode> openQueue, PrioritizedSearchNode psn){
		openQueue.insert(psn);
	}
	
	public void updateOpen(HashIndexedHeap<PrioritizedSearchNode> openQueue, PrioritizedSearchNode openPSN, PrioritizedSearchNode npsn){
		openPSN.setAuxInfoTo(npsn);
		openQueue.refreshPriority(openPSN);
	}
	
	
	@Override
	public void planFromState(State initialState) {
		
		//first determine if there is even a need to plan
		StateHashTuple sih = this.stateHash(initialState);
		
		if(mapToStateIndex.containsKey(sih)){
			return ; //no need to plan since this is already solved
		}
		
		
		//a plan is not cached so being planning process
		this.prePlanPrep();

		HashIndexedHeap<PrioritizedSearchNode> openQueue = new HashIndexedHeap<PrioritizedSearchNode>(new PrioritizedSearchNode.PSNComparator());
		Map<PrioritizedSearchNode, PrioritizedSearchNode> closedSet = new HashMap<PrioritizedSearchNode,PrioritizedSearchNode>();
		
		PrioritizedSearchNode ipsn = new PrioritizedSearchNode(sih, this.computeF(null, null, sih));
		this.insertIntoOpen(openQueue, ipsn);
		
		int nexpanded = 0;
		PrioritizedSearchNode lastVistedNode = null;
		double minF = ipsn.priority;
		while(openQueue.size() > 0){
			
			PrioritizedSearchNode node = openQueue.poll();
			closedSet.put(node, node);
			
			nexpanded++;
			if(node.priority < minF){
				minF = node.priority;
				DPrint.cl(debugCode, "Min F Expanded: " + minF + "; Nodes expanded so far: " + nexpanded + "; Open size: " + openQueue.size());
			}
			
			State s = node.s.s;
			if(gc.satisfies(s)){
				lastVistedNode = node;
				break;
			}
		
			//generate successors
			for(Action a : actions){
				List<GroundedAction> gas = s.getAllGroundedActionsFor(a);
				for(GroundedAction ga : gas){
					State ns = ga.executeIn(s);
					StateHashTuple nsh = this.stateHash(ns);
					
					double F = this.computeF(node, ga, nsh);
					PrioritizedSearchNode npsn = new PrioritizedSearchNode(nsh, ga, node, F);
					
					//check closed
					PrioritizedSearchNode closedPSN = closedSet.get(npsn);
					if(closedPSN != null){
						
						if(F <= closedPSN.priority){
							continue; //no need to reopen because this is a worse path to an already explored node
						}
						
					}
					
					//check open
					PrioritizedSearchNode openPSN = openQueue.containsInstance(npsn);
					if(openPSN == null){
						this.insertIntoOpen(openQueue, npsn);
					}
					else if(F > openPSN.priority){
						this.updateOpen(openQueue, openPSN, npsn);
					}
					
					
				}
				
				
			}
			
			
			
			
		}
		
		//search to goal complete. Now follow back pointers to set policy
		this.encodePlanIntoPolicy(lastVistedNode);
		
		DPrint.cl(debugCode, "Num Expanded: " + nexpanded);
		
		this.postPlanPrep();
		
	}

}
