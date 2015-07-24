package burlap.behavior.singleagent.options;

import java.util.List;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.Policy.ActionProb;
import burlap.oomdp.auxiliary.stateconditiontest.StateConditionTest;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;






/**
 * This is a subgoal option whose initiation states are defined by the state in which the policy is defined. If the agent
 * enters a state outside where the policy is defined, that is a termination state with probability 1, as are any subgoal
 * states.
 * 
 * @author James MacGlashan
 *
 */
public class PolicyDefinedSubgoalOption extends Option {

	Policy						policy;
	StateConditionTest			subgoalTest;
	
	
	/**
	 * Initializes.
	 * @param name the name of the option
	 * @param p the policy of the option
	 * @param sg the subgoals it is meant to reach
	 */
	public PolicyDefinedSubgoalOption(String name, Policy p, StateConditionTest sg){
		this.policy = p;
		this.subgoalTest = sg;
		this.name = name;
		this.parameterClasses = new String[0];
		this.parameterOrderGroup = new String[0];
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
		if(subgoalTest.satisfies(ms) || !this.policy.isDefinedFor(ms)){
			return 1.;
		}
		return 0.;
	}

	@Override
	public boolean applicableInState(State st, String [] params){
		return policy.getAction(this.map(st)) != null;
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
