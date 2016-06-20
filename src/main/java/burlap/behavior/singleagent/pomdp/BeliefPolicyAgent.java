package burlap.behavior.singleagent.pomdp;

import burlap.behavior.policy.Policy;
import burlap.mdp.core.action.Action;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.pomdp.BeliefAgent;
import burlap.mdp.singleagent.pomdp.PODomain;
import burlap.mdp.singleagent.pomdp.beliefstate.BeliefState;


/**
 * A Belief agent that follows a specified policy.
 */
public class BeliefPolicyAgent extends BeliefAgent {

	/**
	 * The policy that the agent will follow.
	 */
	protected Policy policy;


	/**
	 * Initializes.
	 * @param domain the POMDP domain
	 * @param environment the environment with which the agent will interact
	 * @param policy the policy the agent will follow.
	 */
	public BeliefPolicyAgent(PODomain domain, Environment environment, Policy policy){
		super(domain, environment);
		this.policy = policy;
	}
	
	
	@Override
	public Action getAction(BeliefState curBelief) {

		return this.policy.action(curBelief);
	}
	
	

}
