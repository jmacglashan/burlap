package burlap.behavior.singleagent.planning.deterministic;

import java.util.ArrayList;
import java.util.List;

import javax.management.RuntimeErrorException;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.PlannerDerivedPolicy;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;


/**
 * This is a static deterministic planner policy, which means
 * if the source deterministic planner has not already computed
 * and cached the plan for a query state, then this policy
 * is undefined for that state and will not try to compute it
 * @author James MacGlashan
 */


public class SDPlannerPolicy extends Policy implements PlannerDerivedPolicy{

	protected DeterministicPlanner dp;
	
	
	public SDPlannerPolicy(){
		this.dp = null;
	}
	
	
	public SDPlannerPolicy(DeterministicPlanner dp){
		this.dp = dp;
	}
	
	
	@Override
	public void setPlanner(OOMDPPlanner planner) {
		
		if(!(planner instanceof DeterministicPlanner)){
			throw new RuntimeErrorException(new Error("Planner is not a Deterministic Planner"));
		}
		
		this.dp = (DeterministicPlanner)planner;
		
	}
	
	@Override
	public GroundedAction getAction(State s) {
		if(this.dp.cachedPlanForState(s)){
			return this.dp.querySelectedActionForState(s);
		}
		return null; //then the policy is undefined
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		GroundedAction selectedAction = this.getAction(s);
		if(selectedAction == null){
			return null; //policy is undefined for this state
		}
		List <ActionProb> res = new ArrayList<Policy.ActionProb>();
		ActionProb ap = new ActionProb(selectedAction, 1.);
		res.add(ap);
		return res;
	}


	@Override
	public boolean isStochastic() {
		return false;
	}


	

}
