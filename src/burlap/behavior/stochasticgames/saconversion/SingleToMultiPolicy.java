package burlap.behavior.stochasticgames.saconversion;

import java.util.List;

import burlap.behavior.policy.Policy;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.states.State;
import burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction;
import burlap.oomdp.stochasticgames.agentactions.ObParamSGAgentAction.GroundedObParamSGAgentAction;

public class SingleToMultiPolicy extends Policy {

	Policy singlePolicy;
	Domain domain;
	String worldAgentName;

	public SingleToMultiPolicy(Policy singlePolicy, Domain domain,
			String worldAgentName) {
		super();
		this.singlePolicy = singlePolicy;
		this.domain = domain;
		this.worldAgentName = worldAgentName;
	}

	@Override
	public AbstractGroundedAction getAction(State s) {
		
		// GroundedAction to GroundedSingleAction
		AbstractGroundedAction ga = this.singlePolicy.getAction(s);

//		List<GroundedSingleAction> gsas = this.domain.getSingleAction(
//				ga.actionName()).getAllGroundedActionsFor(s, worldAgentName);
//		Random rand = new Random();
//		GroundedSingleAction gsa = gsas.get(rand.nextInt(gsas.size()));
		
		
		GroundedSGAgentAction output = 
				new GroundedObParamSGAgentAction(
						worldAgentName, domain.getSingleAction(ga.actionName()), ga.getParametersAsString());
		return output;
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		return singlePolicy.getActionDistributionForState(s);
	}

	@Override
	public boolean isStochastic() {
		return singlePolicy.isStochastic();
	}

	@Override
	public boolean isDefinedFor(State s) {

		return singlePolicy.isDefinedFor(s);
	}

}
