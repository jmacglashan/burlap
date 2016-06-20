package burlap.behavior.stochasticgames.agents.madp;

import burlap.behavior.stochasticgames.PolicyFromJointPolicy;
import burlap.behavior.stochasticgames.madynamicprogramming.MADynamicProgramming;
import burlap.mdp.stochasticgames.agent.SGAgent;
import burlap.mdp.stochasticgames.agent.AgentFactory;
import burlap.mdp.stochasticgames.SGDomain;
import burlap.mdp.stochasticgames.agent.SGAgentType;


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
	 * @param planner the valueFunction object that will be used by all generated agents
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
	 * @param plannerFactory the valueFunction factory that will be used to generate a valueFunction for the agents
	 * @param policy the policy that will be copied and supplied to all generated objects
	 */
	public MADPPlanAgentFactory(SGDomain domain, MADPPlannerFactory plannerFactory, PolicyFromJointPolicy policy){
		this.domain = domain;
		this.plannerFactory = plannerFactory;
		this.policy = policy;
	}
	
	@Override
	public SGAgent generateAgent(String agentName, SGAgentType type) {
		return new MultiAgentDPPlanningAgent(domain, this.plannerFactory.getPlannerInstance(), this.policy.copy(), agentName, type);
	}

}
