package burlap.behavior.stochasticgames.agents;

import burlap.behavior.policy.Policy;
import burlap.behavior.policy.Policy.ActionProb;
import burlap.oomdp.core.states.State;
import burlap.oomdp.stochasticgames.SGDomain;
import burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction;

public class TransparentSetStrategyAgent extends SetStrategySGAgent{

	public TransparentSetStrategyAgent(SGDomain domain, Policy policy) {
		super(domain, policy);
	}

	@Override
	public GroundedSGAgentAction getAction(State s) {
		System.out.print(super.getAgentName()+"  ");
		for (ActionProb ap : super.policy.getActionDistributionForState(s))
			System.out.print(ap.ga+": "+ap.pSelection+", ");
		System.out.println();
		return super.getAction(s);
	}
	
	public Policy getPolicy(){
		return this.policy;
	}
}
