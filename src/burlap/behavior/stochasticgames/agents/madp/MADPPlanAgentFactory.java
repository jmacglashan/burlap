package burlap.behavior.stochasticgames.agents.madp;

import burlap.behavior.stochasticgames.PolicyFromJointPolicy;
import burlap.behavior.stochasticgames.madynamicprogramming.MADynamicProgramming;
import burlap.oomdp.stochasticgames.SGAgent;
import burlap.oomdp.stochasticgames.AgentFactory;
import burlap.oomdp.stochasticgames.SGDomain;


/**
 * An agent factory for the {@link MultiAgentDPPlanningAgent} agent. Generated agents are always provided a copy of the provided
 * policy object to ensure that multiple agents from the same factory use a policy specific to them.
 * @author James MacGlashan
 *
 */
public class MADPPlanAgentFactory implements AgentFactory {

	protected SGDomain						domain;
	protected MADPPlannerFactory plannerFactory;
	protected PolicyFromJointPolicy			policy;
	
	
	/**
	 * Initializes.
	 * @param domain the domain for the agents
	 * @param planner the planner object that will be used by all generated agents
	 * @param policy the policy that will be copied and supplied to all generated objects
	 */
	public MADPPlanAgentFactory(SGDomain domain, MADynamicProgramming planner, PolicyFromJointPolicy policy){
		this.domain = domain;
		this.plannerFactory = new MADPPlannerFactory.ConstantMADPPlannerFactory(planner);
		this.policy = policy;
	}
	
	
	/**
	 * Initializes
	 * @param domain the domain for the agents
	 * @param plannerFactory the planner factory that will be used to generate a planner for the agents
	 * @param policy the policy that will be copied and supplied to all generated objects
	 */
	public MADPPlanAgentFactory(SGDomain domain, MADPPlannerFactory plannerFactory, PolicyFromJointPolicy policy){
		this.domain = domain;
		this.plannerFactory = plannerFactory;
		this.policy = policy;
	}
	
	@Override
	public SGAgent generateAgent() {
		return new MultiAgentDPPlanningAgent(domain, this.plannerFactory.getPlannerInstance(), this.policy.copy());
	}

}
