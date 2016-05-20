package burlap.behavior.singleagent.options;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.Policy.ActionProb;
import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.auxiliary.stateconditiontest.StateConditionTestIterable;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;

import java.util.List;


/**
 * A class for a classic Markov option. Defined by a Markov policy, an initiation set, and a termination set.
 * @author James MacGlashan
 *
 */
public class SubgoalOption implements Option {


	/**
	 * The name of the option
	 */
	protected String name;

	/**
	 * The policy of the options
	 */
	protected Policy						policy;
	
	/**
	 * The states in which the options can be initiated
	 */
	protected StateConditionTest			initiationTest;
	
	/**
	 * The states in which the option terminates deterministically
	 */
	protected StateConditionTest			terminationStates;
	
	
	
	/**
	 * Initializes.
	 * @param name the name of the option
	 * @param p the option's policy
	 * @param init the initiation states of the option
	 * @param terminationStates the deterministic termination states of the option.
	 */
	public SubgoalOption(String name, Policy p, StateConditionTest init, StateConditionTest terminationStates){
		this.name = name;
		this.policy = p;
		this.initiationTest = init;
		this.terminationStates = terminationStates;
		
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
	public double probabilityOfTermination(State s) {
		if(terminationStates.satisfies(s) || !policy.isDefinedFor(s)){
			return 1.;
		}
		return 0.;
	}

	@Override
	public boolean inInitiationSet(State s) {
		return initiationTest.satisfies(s);
	}

	
	
	@Override
	public void initiateInState(State s) {
		//no bookkeeping
	}

	@Override
	public Action oneStep(State s) {
		return policy.getAction(s);
	}


	@Override
	public List<ActionProb> oneStepProbabilities(State s) {
		return policy.getActionDistributionForState(s);
	}

	@Override
	public EnvironmentOptionOutcome control(Environment env, double discount) {
		return Option.Helper.control(this, env, discount);
	}

	@Override
	public boolean markov() {
		return true;
	}

	@Override
	public String actionName() {
		return this.name;
	}

	@Override
	public Action copy() {
		return new SubgoalOption(name, this.policy, this.initiationTest, this.terminationStates);
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;

		SubgoalOption that = (SubgoalOption) o;

		return name.equals(that.name);

	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return name;
	}
}
