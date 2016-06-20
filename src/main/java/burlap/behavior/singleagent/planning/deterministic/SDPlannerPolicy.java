package burlap.behavior.singleagent.planning.deterministic;

import burlap.behavior.policy.EnumerablePolicy;
import burlap.behavior.policy.SolverDerivedPolicy;
import burlap.behavior.policy.support.ActionProb;
import burlap.behavior.policy.support.PolicyUndefinedException;
import burlap.behavior.singleagent.MDPSolverInterface;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;

import javax.management.RuntimeErrorException;
import java.util.ArrayList;
import java.util.List;


/**
 * This is a static deterministic valueFunction policy, which means
 * if the source deterministic valueFunction has not already computed
 * and cached the plan for a query state, then this policy
 * is undefined for that state and will cause the policy to throw
 * a corresponding {@link PolicyUndefinedException} exception object.
 * @author James MacGlashan
 */


public class SDPlannerPolicy implements SolverDerivedPolicy, EnumerablePolicy {

	protected DeterministicPlanner dp;
	
	
	public SDPlannerPolicy(){
		this.dp = null;
	}
	
	
	public SDPlannerPolicy(DeterministicPlanner dp){
		this.dp = dp;
	}
	
	
	@Override
	public void setSolver(MDPSolverInterface solver) {
		
		if(!(solver instanceof DeterministicPlanner)){
			throw new RuntimeErrorException(new Error("Planner is not a Deterministic Planner"));
		}
		
		this.dp = (DeterministicPlanner) solver;
		
	}
	
	@Override
	public Action action(State s) {
		
		if(this.dp == null){
			throw new RuntimeException("The valueFunction used by this Policy is not defined; therefore, the policy is undefined.");
		}
		
		if(this.dp.hasCachedPlanForState(s)){
			Action ga = this.dp.querySelectedActionForState(s);
			//the surrounding if condition will probably be sufficient for null cases, but doing double check just to make sure.
			if(ga == null){
				throw new PolicyUndefinedException();
			}
			return ga;
		}
		throw new PolicyUndefinedException();
	}

	@Override
	public double actionProb(State s, Action a) {
		if(a.equals(this.action(s))){
			return 1.;
		}
		return 0.;
	}

	@Override
	public List<ActionProb> policyDistribution(State s) {
		Action selectedAction = this.action(s);
		if(selectedAction == null){
			throw new PolicyUndefinedException();
		}
		List <ActionProb> res = new ArrayList<ActionProb>();
		ActionProb ap = new ActionProb(selectedAction, 1.);
		res.add(ap);
		return res;
	}



	@Override
	public boolean definedFor(State s) {
		if(this.dp == null){
			throw new RuntimeException("The valueFunction used by this Policy is not defined; therefore, the policy is undefined.");
		}
		if(this.dp.hasCachedPlanForState(s)){
			return true;
		}
		
		return false;
	}


	

}
