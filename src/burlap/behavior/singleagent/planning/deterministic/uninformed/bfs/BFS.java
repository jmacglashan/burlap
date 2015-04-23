package burlap.behavior.singleagent.planning.deterministic.uninformed.bfs;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.deterministic.DeterministicPlanner;
import burlap.behavior.singleagent.planning.deterministic.SearchNode;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.debugtools.DPrint;
import burlap.oomdp.auxiliary.common.NullTermination;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.common.UniformCostRF;

/**
 * Implements Breadth-first search.
 * 
 * <p/>
 * If a terminal function is provided via the setter method defined for OO-MDPs, then the search algorithm will not expand any nodes
 * that are terminal states, as if there were no actions that could be executed from that state. Note that terminal states
 * are not necessarily the same as goal states, since there could be a fail condition from which the agent cannot act, but
 * that is not explicitly represented in the transition dynamics.
 * 
 * 
 * @author James MacGlashan
 *
 */
public class BFS extends DeterministicPlanner {

	
	/**
	 * BFS only needs reference to the domain, goal conditions, and hashing factory. The reward function is considered UniformCost, but is
	 * not used. No states are considered terminal states, but planning will stop when it finds the goal state.
	 * @param domain the domain in which to plan
	 * @param gc the test for goal states
	 * @param hashingFactory the state hashing factory to use.
	 */
	public BFS(Domain domain, StateConditionTest gc, StateHashFactory hashingFactory){
		this.deterministicPlannerInit(domain, new UniformCostRF(), new NullTermination(), gc, hashingFactory);
	}
	
	
	@Override
	public void planFromState(State initialState) {
		
		StateHashTuple sih = this.stateHash(initialState);
		
		if(mapToStateIndex.containsKey(sih)){
			return ; //no need to plan since this is already solved
		}
		
		
		LinkedList<SearchNode> openQueue = new LinkedList<SearchNode>();
		Set <SearchNode> openedSet = new HashSet<SearchNode>();
		
		
		SearchNode initialSearchNode = new SearchNode(sih);
		openQueue.offer(initialSearchNode);
		openedSet.add(initialSearchNode);
		
		SearchNode lastVistedNode = null;
		
		
		int nexpanded = 0;
		while(openQueue.size() > 0){
			
			SearchNode node = openQueue.poll();
			nexpanded++;
			
			
			
			State s = node.s.s;
			if(gc.satisfies(s)){
				lastVistedNode = node;
				break;
			}
			
			if(this.tf.isTerminal(s)){
				continue; //don't expand terminal states
			}
			
			//first get all grounded actions for this state
			/*List <GroundedAction> gas = new ArrayList<GroundedAction>();
			for(Action a : actions){
				gas.addAll(s.getAllGroundedActionsFor(a));
				
			}
			*/
			List<GroundedAction> gas = Action.getAllApplicableGroundedActionsFromActionList(this.actions, s);
			
			
			//add children reach from each deterministic action
			for(GroundedAction ga : gas){
				State ns = ga.executeIn(s);
				StateHashTuple nsh = this.stateHash(ns);
				SearchNode nsn = new SearchNode(nsh, ga, node);
				
				if(openedSet.contains(nsn)){
					continue;
				}
				
				//otherwise add for expansion
				openQueue.offer(nsn);
				openedSet.add(nsn);
				
				
			}
			
			
		}
		
	
		
		this.encodePlanIntoPolicy(lastVistedNode);

		
		DPrint.cl(debugCode,"Num Expanded: " + nexpanded);
		
	}

}
