package burlap.behavior.stochasticgame.agents;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.Policy.ActionProb;
import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.GroundedSingleAction;
import burlap.oomdp.stochasticgames.SGDomain;

public class TransparentSetStrategyAgent extends SetStrategyAgent{

	public TransparentSetStrategyAgent(SGDomain domain, Policy policy) {
		super(domain, policy);
	}

	@Override
	public GroundedSingleAction getAction(State s) {
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
