package burlap.oomdp.stocashticgames.common;

import burlap.oomdp.stocashticgames.Agent;
import burlap.oomdp.stocashticgames.AgentFactory;
import burlap.oomdp.stocashticgames.JointReward;

public class AgentFactoryWithSubjectiveReward implements AgentFactory {

	protected AgentFactory			baseFactory;
	protected JointReward			internalReward;
	
	public AgentFactoryWithSubjectiveReward(AgentFactory baseFactory, JointReward internalReward) {
		this.baseFactory = baseFactory;
		this.internalReward = internalReward;
	}

	@Override
	public Agent generateAgent() {
		Agent a = baseFactory.generateAgent();
		a.setInternalRewardFunction(internalReward);
		return a;
	}

}
