package burlap.behavior.singleagent.planning.deterministic;

import java.util.ArrayList;
import java.util.List;

import javax.management.RuntimeErrorException;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.PlannerDerivedPolicy;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;


/**
 * This is a static deterministic planner policy, which means
 * if the source deterministic planner has not already computed
 * and cached the plan for a query state, then this policy
 * is undefined for that state and will cause the policy to throw
 * a corresponding {@link burlap.behavior.policy.Policy.PolicyUndefinedException} exception object.
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
	public AbstractGroundedAction getAction(State s) {
		
		if(this.dp == null){
			throw new RuntimeException("The planner used by this Policy is not defined; therefore, the policy is undefined.");
		}
		
		if(this.dp.hasCachedPlanForState(s)){
			GroundedAction ga = this.dp.querySelectedActionForState(s);
			//the surrounding if condition will probably be sufficient for null cases, but doing double check just to make sure.
			if(ga == null){
				throw new PolicyUndefinedException();
			}
			return ga;
		}
		throw new PolicyUndefinedException();
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		GroundedAction selectedAction = (GroundedAction)this.getAction(s);
		if(selectedAction == null){
			throw new PolicyUndefinedException();
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


	@Override
	public boolean isDefinedFor(State s) {
		if(this.dp == null){
			throw new RuntimeException("The planner used by this Policy is not defined; therefore, the policy is undefined.");
		}
		if(this.dp.hasCachedPlanForState(s)){
			return true;
		}
		
		return false;
	}


	

}
