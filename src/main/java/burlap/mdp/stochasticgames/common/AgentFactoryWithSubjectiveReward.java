package burlap.mdp.stochasticgames.common;

import burlap.mdp.stochasticgames.agent.SGAgent;
import burlap.mdp.stochasticgames.agent.AgentFactory;
import burlap.mdp.stochasticgames.agent.SGAgentBase;
import burlap.mdp.stochasticgames.agent.SGAgentType;
import burlap.mdp.stochasticgames.model.JointRewardFunction;

/**
 * An agent generating factory that will produce an agent that uses an internal subjective reward function.
 * This can be useful for agents that use reward shaping. The base agent is first generated using
 * a different {@link AgentFactory} and the returned agent from
 * that provided agent has its internal reward function set to the one specified for use
 * in this factory. The agent is then returned by this factory.
 * @author James MacGlashan
 *
 */
public class AgentFactoryWithSubjectiveReward implements AgentFactory {

	protected AgentFactory			baseFactory;
	protected JointRewardFunction internalReward;
	
	
	/**
	 * Initializes the factory.
	 * @param baseFactory the base factory for generating an agent.
	 * @param internalReward the internal reward function to set the agent to use.
	 */
	public AgentFactoryWithSubjectiveReward(AgentFactory baseFactory, JointRewardFunction internalReward) {
		this.baseFactory = baseFactory;
		this.internalReward = internalReward;
	}

	@Override
	public SGAgent generateAgent(String agentName, SGAgentType type) {
		SGAgentBase a = (SGAgentBase)baseFactory.generateAgent(agentName, type);
		a.setInternalRewardFunction(internalReward);
		return a;
	}

}
