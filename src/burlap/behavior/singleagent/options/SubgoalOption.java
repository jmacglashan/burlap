package burlap.behavior.singleagent.options;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.support.ActionProb;
import burlap.behavior.singleagent.Episode;
import burlap.mdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.mdp.auxiliary.stateconditiontest.StateConditionTestIterable;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;

import java.util.List;


/**
 * A class for a classic subgoal Markov option. The option policy is specified with a {@link Policy} object,
 * the deterministic termination conditions with a {@link StateConditionTest} and the initiation set with a
 * {@link StateConditionTest}.
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
	 * A default constructor for serialization purposes. In general, you should use the {@link #SubgoalOption(String, Policy, StateConditionTest, StateConditionTest)}
	 * constructor.
	 */
	public SubgoalOption() {
	}

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



	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Policy getPolicy() {
		return policy;
	}

	public void setPolicy(Policy policy) {
		this.policy = policy;
	}

	public void setInitiationTest(StateConditionTest initiationTest) {
		this.initiationTest = initiationTest;
	}

	public StateConditionTest getTerminationStates() {
		return terminationStates;
	}

	public void setTerminationStates(StateConditionTest terminationStates) {
		this.terminationStates = terminationStates;
	}

	/**
	 * Returns true if the initiation states and termination states of this option are iterable; false if either of them are not.
	 * @return true if the initiation states and termination states of this option are iterable; false if either of them are not.
	 */
	public boolean enumerable(){
		return (initiationTest instanceof StateConditionTestIterable) && (terminationStates instanceof StateConditionTestIterable);
	}



	@Override
	public double probabilityOfTermination(State s, Episode history) {
		if(terminationStates.satisfies(s) || !policy.definedFor(s)){
			return 1.;
		}
		return 0.;
	}

	@Override
	public boolean inInitiationSet(State s) {
		return initiationTest.satisfies(s);
	}



	@Override
	public Action policy(State s, Episode history) {
		return policy.action(s);
	}


	@Override
	public List<ActionProb> policyDistribution(State s, Episode history) {
		return policy.policyDistribution(s);
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
