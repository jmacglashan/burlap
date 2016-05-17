package burlap.behavior.singleagent.planning.deterministic.uninformed.bfs;

import burlap.behavior.singleagent.planning.deterministic.DeterministicPlanner;
import burlap.behavior.singleagent.planning.deterministic.SDPlannerPolicy;
import burlap.behavior.singleagent.planning.deterministic.SearchNode;
import burlap.debugtools.DPrint;
import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.action.ActionUtils;
import burlap.mdp.singleagent.SADomain;
import burlap.statehashing.HashableState;
import burlap.statehashing.HashableStateFactory;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Implements Breadth-first search.
 * 
 * <p>
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
	public BFS(SADomain domain, StateConditionTest gc, HashableStateFactory hashingFactory){
		this.deterministicPlannerInit(domain, gc, hashingFactory);
	}


	/**
	 * Plans and returns a {@link burlap.behavior.singleagent.planning.deterministic.SDPlannerPolicy}. If
	 * a {@link State} is not in the solution path of this planner, then
	 * the {@link burlap.behavior.singleagent.planning.deterministic.SDPlannerPolicy} will throw
	 * a runtime exception. If you want a policy that will dynamically replan for unknown states,
	 * you should create your own {@link burlap.behavior.singleagent.planning.deterministic.DDPlannerPolicy}.
	 * @param initialState the initial state of the planning problem
	 * @return a {@link burlap.behavior.singleagent.planning.deterministic.SDPlannerPolicy}.
	 */
	@Override
	public SDPlannerPolicy planFromState(State initialState) {
		
		HashableState sih = this.stateHash(initialState);
		
		if(internalPolicy.containsKey(sih)){
			return new SDPlannerPolicy(this); //no need to plan since this is already solved
		}
		
		
		LinkedList<SearchNode> openQueue = new LinkedList<SearchNode>();
		Set <SearchNode> openedSet = new HashSet<SearchNode>();
		
		
		SearchNode initialSearchNode = new SearchNode(sih);
		openQueue.offer(initialSearchNode);
		openedSet.add(initialSearchNode);
		
		SearchNode lastVistedNode = null;
		
		
		int nexpanded = 0;
		while(!openQueue.isEmpty()){
			
			SearchNode node = openQueue.poll();
			nexpanded++;
			
			
			
			State s = node.s.s;
			if(gc.satisfies(s)){
				lastVistedNode = node;
				break;
			}
			
			if(this.model.terminal(s)){
				continue; //don't expand terminal states
			}
			
			//first get all grounded actions for this state
			List<Action> gas = ActionUtils.allApplicableActionsForTypes(this.actionTypes, s);
			
			
			//add children reach from each deterministic action
			for(Action ga : gas){
				State ns = this.model.sample(s, ga).op;
				HashableState nsh = this.stateHash(ns);
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

		return new SDPlannerPolicy(this);
		
	}

}
