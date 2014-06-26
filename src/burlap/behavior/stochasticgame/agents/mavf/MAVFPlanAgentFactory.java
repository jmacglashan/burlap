package burlap.behavior.stochasticgame.agents.mavf;

import burlap.behavior.stochasticgame.PolicyFromJointPolicy;
import burlap.behavior.stochasticgame.mavaluefunction.MAValueFunctionPlanner;
import burlap.oomdp.stochasticgames.Agent;
import burlap.oomdp.stochasticgames.AgentFactory;
import burlap.oomdp.stochasticgames.SGDomain;


/**
 * An agent factory for the {@link MultiAgentVFPlanningAgent} agent. Generated agents are always provided a copy of the provided
 * policy object to ensure that multiple agents from the same factory use a policy specific to them.
 * @author James MacGlashan
 *
 */
public class MAVFPlanAgentFactory implements AgentFactory {

	protected SGDomain						domain;
	protected MAVFPlannerFactory			plannerFactory;
	protected PolicyFromJointPolicy			policy;
	
	
	/**
	 * Initializes.
	 * @param domain the domain for the agents
	 * @param planner the planner object that will be used by all generated agents
	 * @param policy the policy that will be copied and supplied to all generated objects
	 */
	public MAVFPlanAgentFactory(SGDomain domain, MAValueFunctionPlanner planner, PolicyFromJointPolicy policy){
		this.domain = domain;
		this.plannerFactory = new MAVFPlannerFactory.ConstantMAVFPlannerFactory(planner);
		this.policy = policy;
	}
	
	
	/**
	 * Initializes
	 * @param domain the domain for the agents
	 * @param plannerFactory the planner factory that will be used to generate a planner for the agents
	 * @param policy the policy that will be copied and supplied to all generated objects
	 */
	public MAVFPlanAgentFactory(SGDomain domain, MAVFPlannerFactory plannerFactory, PolicyFromJointPolicy policy){
		this.domain = domain;
		this.plannerFactory = plannerFactory;
		this.policy = policy;
	}
	
	@Override
	public Agent generateAgent() {
		return new MultiAgentVFPlanningAgent(domain, this.plannerFactory.getPlannerInstance(), this.policy.copy());
	}

}
