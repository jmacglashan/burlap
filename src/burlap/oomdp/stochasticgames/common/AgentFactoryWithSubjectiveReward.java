package burlap.oomdp.stochasticgames.common;

import burlap.oomdp.stochasticgames.SGAgent;
import burlap.oomdp.stochasticgames.AgentFactory;
import burlap.oomdp.stochasticgames.JointReward;

/**
 * An agent generating factory that will produce an agent that uses an internal subjective reward function.
 * This can be useful for agents that use reward shaping. The base agent is first generated using
 * a different {@link burlap.oomdp.stochasticgames.AgentFactory} and the returned agent from
 * that provided agent has its internal reward function set to the one specified for use
 * in this factory. The agent is then returned by this factory.
 * @author James MacGlashan
 *
 */
public class AgentFactoryWithSubjectiveReward implements AgentFactory {

	protected AgentFactory			baseFactory;
	protected JointReward			internalReward;
	
	
	/**
	 * Initializes the factory.
	 * @param baseFactory the base factory for generating an agent.
	 * @param internalReward the internal reward function to set the agent to use.
	 */
	public AgentFactoryWithSubjectiveReward(AgentFactory baseFactory, JointReward internalReward) {
		this.baseFactory = baseFactory;
		this.internalReward = internalReward;
	}

	@Override
	public SGAgent generateAgent() {
		SGAgent a = baseFactory.generateAgent();
		a.setInternalRewardFunction(internalReward);
		return a;
	}

}
