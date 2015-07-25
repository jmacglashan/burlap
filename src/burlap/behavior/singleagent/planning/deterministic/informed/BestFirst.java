package burlap.behavior.singleagent.planning.deterministic.informed;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.behavior.singleagent.planning.deterministic.DeterministicPlanner;
import burlap.behavior.singleagent.planning.deterministic.SDPlannerPolicy;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.datastructures.HashIndexedHeap;
import burlap.debugtools.DPrint;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;


/**
 * An abstract class for implementing Best-first search planning. Best-first search planning stores children node in a priority queue
 * where the priority of nodes is indicated by an f-function. Different Best-first search planning algorithms are implemented
 * by changing how the f-score is computed, which is an abstract method of this class that needs to be override. A*, for instance,
 * is a Best-first valueFunction in which the f-score is g(s) + h(s), where g(s) is the cost to state s, and h(s) is the heuristic score for s.
 * Best-first search requires checking if the open queue already has a search node stored in it and possibly modifying its priority and back pointers
 * if a better path to it has been found. To efficiently provide that functionality, this class makes use of a custom
 * hash-backed priority queue (heap) which performs contains tests using a hash map.
 * <p/>
 * If a terminal function is provided to subclasses of BestFirst, then the BestFirst search algorithm will not expand any nodes
 * that are terminal states, as if there were no actions that could be executed from that state. Note that terminal states
 * are not necessarily the same as goal states, since there could be a fail condition from which the agent cannot act, but
 * that is not explicitly represented in the transition dynamics.
 * @author James MacGlashan
 *
 */
public abstract class BestFirst extends DeterministicPlanner {

	
	/**
	 * This method returns the f-score for a state given the parent search node, the generating action, the state that was produced.
	 * @param parentNode the parent search node (and its priority) that from which the next state was generated.
	 * @param generatingAction the action that was used to generate the next state.
	 * @param successorState the next state that was generated
	 * @return the f-score for the next state.
	 */
	public abstract double computeF(PrioritizedSearchNode parentNode, GroundedAction generatingAction, StateHashTuple successorState);
	
	
	/**
	 * This method is called at the start of the {@link #planFromState(State)} method and can be used initialize any special
	 * data structures needed by the subclass. By default it does nothing.
	 */
	public void prePlanPrep(){
		//do nothing for default
	}
	
	
	/**
	 * This method is called at the end of the {@link #planFromState(State)} method and can be used clean up any special
	 * data structures needed by the subclass. By default it does nothing.
	 */
	public void postPlanPrep(){
		//do nothing for default
	}
	
	
	/**
	 * This method is used to insert a prioritized search node into the openQueue. If the subclass needs
	 * to do special procedures on his insert (such as using a subclass of {@link PrioritizedSearchNode} with more information),
	 * it can override it.
	 * @param openQueue the open queue in which the search node will be inserted.
	 * @param psn the search node to insert.
	 */
	public void insertIntoOpen(HashIndexedHeap<PrioritizedSearchNode> openQueue, PrioritizedSearchNode psn){
		openQueue.insert(psn);
	}
	
	
	/**
	 * This method is called whenever a search node already in the openQueue needs to have its information or priority updated to reflect a new search node.
	 * If a subclass needs to handle special procedures (such as using a subclass of {@link PrioritizedSearchNode} with more information), it can override this method.
	 * @param openQueue the open queue in which the search node exists.
	 * @param openPSN the search node indexed in the open queue that will be updated.
	 * @param npsn the new search node that contains the updated information.
	 */
	public void updateOpen(HashIndexedHeap<PrioritizedSearchNode> openQueue, PrioritizedSearchNode openPSN, PrioritizedSearchNode npsn){
		openPSN.setAuxInfoTo(npsn);
		openQueue.refreshPriority(openPSN);
	}


	/**
	 * Plans and returns a {@link burlap.behavior.singleagent.planning.deterministic.SDPlannerPolicy}. If
	 * a {@link burlap.oomdp.core.states.State} is not in the solution path of this planner, then
	 * the {@link burlap.behavior.singleagent.planning.deterministic.SDPlannerPolicy} will throw
	 * a runtime exception. If you want a policy that will dynamically replan for unknown states,
	 * you should create your own {@link burlap.behavior.singleagent.planning.deterministic.DDPlannerPolicy}.
	 * @param initialState the initial state of the planning problem
	 * @return a {@link burlap.behavior.singleagent.planning.deterministic.SDPlannerPolicy}.
	 */
	@Override
	public SDPlannerPolicy planFromState(State initialState) {
		
		//first determine if there is even a need to plan
		StateHashTuple sih = this.stateHash(initialState);
		
		if(mapToStateIndex.containsKey(sih)){
			return new SDPlannerPolicy(this); //no need to plan since this is already solved
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
			
			if(this.tf.isTerminal(s)){
				continue; //do not expand nodes from a terminal state
			}
		
			//generate successors
			for(Action a : actions){
				//List<GroundedAction> gas = s.getAllGroundedActionsFor(a);
				List<GroundedAction> gas = a.getAllApplicableGroundedActions(s);
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

		return new SDPlannerPolicy(this);
		
	}

}
