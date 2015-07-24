package burlap.behavior.singleagent.planning.stochastic.montecarlo.uct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.policy.PlannerDerivedPolicy;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;



/**
 * This policy is for use with UCT. Note that UCT can only guarantee the policy for the initial state of planning.
 * However, the policy from states that lie on the greedy path from the initial state are likely "okay" as well
 * since they were used for the determining which action to take in the initial state.
 * 
 * This class defines the policy for states that lie along the the greedy path of the UCT
 * tree. Any state not visited by the greedy path in the UCT tree is excluded from the policy and will result
 * in an error if this policy is queried for such a state.
 *  
 * A more robust policy would cause the planner to be called at each state to build a new tree.
 * @author James MacGlashan
 *
 */
public class UCTTreeWalkPolicy extends Policy implements PlannerDerivedPolicy{

	UCT 									planner;
	
	Map<StateHashTuple, GroundedAction> 	policy;
	
	/**
	 * Initializes the policy with the UCT planner
	 * @param planner the UCT planner whose tree should be walked.
	 */
	public UCTTreeWalkPolicy(UCT planner){
		this.planner = planner;
		policy = null;
	}
	
	@Override
	public void setPlanner(OOMDPPlanner planner) {
		if(!(planner instanceof UCT)){
			throw new RuntimeException("Planner must be an instance of UCT");
		}
		this.planner = (UCT)planner;
		
	}
	
	
	/**
	 * computes a hash-backed policy for every state visited along the greedy path of the UCT tree.
	 */
	public void computePolicyFromTree(){
		policy = new HashMap<StateHashTuple, GroundedAction>();

		if(this.planner.getRoot() == null){
			return ;
		}
		
		//define policy for all states that are expanded along the greedy path of the UCT tree
		LinkedList<UCTStateNode> queue = new LinkedList<UCTStateNode>();
		queue.add(planner.getRoot());
		while(queue.size() > 0){
			
			UCTStateNode snode = queue.poll();
			
			if(!planner.containsActionPreference(snode)){
				System.out.println("UCT tree does not contain action preferences of the state queried by the UCTTreeWalkPolicy. Consider replanning with planFromState");
				break; //policy ill defined
			}
			
			UCTActionNode choice = this.getQGreedyNode(snode);
			if(choice != null){
				
				policy.put(snode.state, choice.action); //set the policy
				
				List <UCTStateNode> successors = choice.getAllSuccessors(); //queue up all possible successors of this action
				for(UCTStateNode suc : successors){
					queue.offer(suc);
				}
				
			}
			
		}
		
	}
	
	
	/**
	 * Returns the {@link UCTActionNode} with the highest average sample return. Note that this does not use the upper confidence since
	 * planning is completed.
	 * @param snode the {@link UCTStateNode} for which to get the best {@link UCTActionNode}.
	 * @return the {@link UCTActionNode} with the highest average sample return.
	 */
	protected UCTActionNode getQGreedyNode(UCTStateNode snode){
		
		double maxQ = Double.NEGATIVE_INFINITY;
		UCTActionNode choice = null;
		
		for(UCTActionNode anode : snode.actionNodes){
			
			//only select nodes that have been visited
			if(anode.n > 0 && anode.averageReturn() > maxQ){
				maxQ = anode.averageReturn();
				choice = anode;
			}
			
		}
		
		return choice;
		
	}
	
	@Override
	public AbstractGroundedAction getAction(State s) {
		
		if(policy == null){
			this.computePolicyFromTree();
		}
		
		GroundedAction ga = policy.get(planner.stateHash(s));
		if(ga == null){
			throw new PolicyUndefinedException();
		}
		
		return ga;
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		
		if(policy == null){
			this.computePolicyFromTree();
		}
		
		GroundedAction ga = policy.get(planner.stateHash(s));
		if(ga == null){
			throw new PolicyUndefinedException();
		}
		
		List <ActionProb> res = new ArrayList<Policy.ActionProb>();
		res.add(new ActionProb(ga, 1.)); //greedy policy so only need to supply the mapped action
		
		return res;
	}


	@Override
	public boolean isStochastic() {
		return false; //although UCT solves stochastic MDPs, the policy returned here is deterministic and greedy
	}

	@Override
	public boolean isDefinedFor(State s) {
		if(policy == null){
			this.computePolicyFromTree();
		}
		GroundedAction ga = policy.get(planner.stateHash(s));
		if(ga == null){
			return false;
		}
		
		return true;
	}


	

}
