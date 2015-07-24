package burlap.behavior.singleagent.planning.deterministic;

import java.util.ArrayList;
import java.util.List;

import javax.management.RuntimeErrorException;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.PlannerDerivedPolicy;
import burlap.behavior.singleagent.planning.deterministic.DeterministicPlanner.PlanningFailedException;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;



/**
 * This is a dynamic deterministic planner policy, which means
 * if the source deterministic planner has not already computed
 * and cached the plan for a query state, then this policy
 * will first compute a plan using the planner and then return the
 * answer
 * @author James MacGlashan
 */
public class DDPlannerPolicy extends Policy implements PlannerDerivedPolicy{

	protected DeterministicPlanner dp;
	
	
	public DDPlannerPolicy(){
		this.dp = null;
	}
	
	/**
	 * Initializes with the deterministic planner
	 * @param dp the deterministic planner to use for policy generation
	 */
	public DDPlannerPolicy(DeterministicPlanner dp){
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
		return dp.querySelectedActionForState(s);
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		GroundedAction selectedAction = (GroundedAction)this.getAction(s);
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
		GroundedAction ga = null;
		try{
			ga = dp.querySelectedActionForState(s);
		}catch(PlanningFailedException e){
			//do nothing
		}
		if(ga != null){
			return true;
		}
		
		return false;
	}

}
