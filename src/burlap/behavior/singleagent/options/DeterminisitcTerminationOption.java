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


/**
 * An option with a defined policy, initiation state set and termination state set. This option
 * has deterministic termination conditions in any state where the policy is undefined, or in any state
 * that is in the termination set. In general, this method should be used instead of the {@link PolicyDefinedSubgoalOption}
 * when the client needs more control over in which states the option can be initiated and in which states it terminates.
 * One other advantage of this option is that it can be passed a planner from which its policy will be created. The planner
 * will be called from each initiation state provided to the option constructor.
 * @author James MacGlashan
 *
 */
public class DeterminisitcTerminationOption extends Option {

	/**
	 * The policy of the options
	 */
	Policy						policy;
	
	/**
	 * The states in which the options can be initiated
	 */
	StateConditionTest			initiationTest;
	
	/**
	 * The states in which the option terminates deterministically
	 */
	StateConditionTest			terminationStates;
	
	
	
	/**
	 * Initializes.
	 * @param name the name of the option
	 * @param p the option's policy
	 * @param init the initiation states of the option
	 * @param terminationStates the deterministic termination states of the option.
	 */
	public DeterminisitcTerminationOption(String name, Policy p, StateConditionTest init, StateConditionTest terminationStates){
		this.name = name;
		this.policy = p;
		this.initiationTest = init;
		this.terminationStates = terminationStates;
		
		this.parameterClasses = new String[0];
		this.parameterOrderGroup = new String[0];
		
	}
	
	
	/**
	 * Initializes the option by creating the policy uses some provided option. The planner is called repeatedly on each state in the
	 * initiation state set (which needs to be a {@link burlap.behavior.singleagent.planning.StateConditionTestIterable}) and then
	 * sets this options policy to the planner derived policy that is provided.
	 * @param name the name of the option
	 * @param init the iterable initiation states
	 * @param terminaitonStates the termination states of the option
	 * @param planner the planner to be used to create the policy for this option
	 * @param p the planner derived policy to use after planning from each initial state is performed.
	 */
	public DeterminisitcTerminationOption(String name, StateConditionTestIterable init, StateConditionTest terminaitonStates, OOMDPPlanner planner, PlannerDerivedPolicy p){
		
		if(!(p instanceof Policy)){
			throw new RuntimeErrorException(new Error("PlannerDerivedPolicy p is not an instnace of Policy"));
		}
		
		
		this.name = name;
		
		this.initiationTest = init;
		this.terminationStates = terminaitonStates;
		
		this.parameterClasses = new String[0];
		this.parameterOrderGroup = new String[0];
		
		//now construct the policy using the planner from each possible initiation state
		for(State si : init){
			planner.planFromState(si);
		}
		
		p.setPlanner(planner);
		this.policy = (Policy)p;
		
	}
	
	/**
	 * Initializes the option by creating the policy uses some provided option. The planner is called repeatedly on each state in the
	 * the list {@link seedStatesForPlanning} and then
	 * sets this options policy to the planner derived policy that is provided.
	 * @param name the name of the option
	 * @param init the initiation conditions of the option
	 * @param terminationStates the termination states of the option
	 * @param seedStatesForPlanning the states that should be used as initial states for the planner
	 * @param planner the planner that is used to create the policy for this option
	 * @param p the planner derived policy to use after planning from each initial state is performed.
	 */
	public DeterminisitcTerminationOption(String name, StateConditionTest init, StateConditionTest terminationStates, List <State> seedStatesForPlanning, 
			OOMDPPlanner planner, PlannerDerivedPolicy p){
		
		if(!(p instanceof Policy)){
			throw new RuntimeErrorException(new Error("PlannerDerivedPolicy p is not an instnace of Policy"));
		}
		
		
		this.name = name;
		
		this.initiationTest = init;
		this.terminationStates = terminationStates;
		
		this.parameterClasses = new String[0];
		this.parameterOrderGroup = new String[0];
		
		//now construct the policy using the planner from each possible initiation state
		for(State si : seedStatesForPlanning){
			planner.planFromState(si);
		}
		
		p.setPlanner(planner);
		this.policy = (Policy)p;
		
	}
	
	
	
	/**
	 * Returns the object defining the initiation states.
	 * @return the object defining the initiation states.
	 */
	public StateConditionTest getInitiationTest(){
		return this.initiationTest;
	}
	
	
	/**
	 * Returns the object defining the termination states.
	 * @return the object defining the termination states.
	 */
	public StateConditionTest getTerminiationStates(){
		return this.terminationStates;
	}
	
	
	/**
	 * Returns true if the initiation states and termination states of this option are iterable; false if either of them are not.
	 * @return true if the initiation states and termination states of this option are iterable; false if either of them are not.
	 */
	public boolean enumerable(){
		return (initiationTest instanceof StateConditionTestIterable) && (terminationStates instanceof StateConditionTestIterable);
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
		if(terminationStates.satisfies(ms) || policy.isDefinedFor(ms)){
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
		return (GroundedAction)policy.getAction(this.map(s));
	}


	@Override
	public List<ActionProb> getActionDistributionForState(State s, String[] params) {
		return policy.getActionDistributionForState(this.map(s));
	}

	
	
	
}
