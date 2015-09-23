package burlap.behavior.singleagent.pomdp;

import burlap.behavior.policy.Policy;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.pomdp.BeliefAgent;
import burlap.oomdp.singleagent.pomdp.beliefstate.BeliefState;
import burlap.oomdp.singleagent.pomdp.PODomain;


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
	public GroundedAction getAction(BeliefState curBelief) {

		GroundedAction ga = (GroundedAction)this.policy.getAction(curBelief);
		return ga;
	}
	
	

}
