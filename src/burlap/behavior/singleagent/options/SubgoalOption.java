package burlap.behavior.singleagent.options;

import java.util.List;

import javax.management.RuntimeErrorException;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.Policy.ActionProb;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.PlannerDerivedPolicy;
import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.StateConditionTestIterable;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;


public class SubgoalOption extends Option {

	Policy						policy;
	StateConditionTest			initiationTest;
	StateConditionTest			subgoalTest;
	
	
	public SubgoalOption(String name, Policy p, StateConditionTest init, StateConditionTest sg){
		this.name = name;
		this.policy = p;
		this.initiationTest = init;
		this.subgoalTest = sg;
		
		this.parameterClasses = new String[0];
		this.parameterOrderGroup = new String[0];
		
	}
	
	
	public SubgoalOption(String name, StateConditionTestIterable init, StateConditionTest sg, OOMDPPlanner planner, PlannerDerivedPolicy p){
		
		if(!(p instanceof Policy)){
			throw new RuntimeErrorException(new Error("PlannerDerivedPolicy p is not an instnace of Policy"));
		}
		
		
		this.name = name;
		
		this.initiationTest = init;
		this.subgoalTest = sg;
		
		this.parameterClasses = new String[0];
		this.parameterOrderGroup = new String[0];
		
		//now construct the policy using the planner from each possible initiation state
		for(State si : init){
			planner.planFromState(si);
		}
		
		p.setPlanner(planner);
		this.policy = (Policy)p;
		
	}
	
	
	public SubgoalOption(String name, StateConditionTest init, StateConditionTest sg, List <State> seedStatesForPlanning, 
			OOMDPPlanner planner, PlannerDerivedPolicy p){
		
		if(!(p instanceof Policy)){
			throw new RuntimeErrorException(new Error("PlannerDerivedPolicy p is not an instnace of Policy"));
		}
		
		
		this.name = name;
		
		this.initiationTest = init;
		this.subgoalTest = sg;
		
		this.parameterClasses = new String[0];
		this.parameterOrderGroup = new String[0];
		
		//now construct the policy using the planner from each possible initiation state
		for(State si : seedStatesForPlanning){
			planner.planFromState(si);
		}
		
		p.setPlanner(planner);
		this.policy = (Policy)p;
		
	}
	
	
	
	public StateConditionTest getInitiationTest(){
		return this.initiationTest;
	}
	
	public StateConditionTest getSubgoalTest(){
		return this.subgoalTest;
	}
	
	public boolean enumerable(){
		return (initiationTest instanceof StateConditionTestIterable) && (subgoalTest instanceof StateConditionTestIterable);
	}
	
	@Override
	public boolean isMarkov() {
		return true;
	}

	@Override
	public boolean usesDeterministicTermination() {
		return true;
	}

	@Override
	public boolean usesDeterministicPolicy() {
		return !policy.isStochastic();
	}

	@Override
	public double probabilityOfTermination(State s, String[] params) {
		State ms = this.map(s);
		if(subgoalTest.satisfies(ms) || policy.getAction(ms) == null){
			return 1.;
		}
		return 0.;
	}

	
	
	@Override
	public boolean applicableInState(State st, String [] params){
		if(initiationTest.satisfies(this.map(st))){
			return true;
		}
		return false;
	}
	
	
	@Override
	public void initiateInStateHelper(State s, String[] params) {
		//no bookkeeping
	}

	@Override
	public GroundedAction oneStepActionSelection(State s, String[] params) {
		return policy.getAction(this.map(s));
	}


	@Override
	public List<ActionProb> getActionDistributionForState(State s, String[] params) {
		return policy.getActionDistributionForState(this.map(s));
	}

	
	
	
}
