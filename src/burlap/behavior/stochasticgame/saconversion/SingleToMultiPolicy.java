package burlap.behavior.stochasticgame.saconversion;

import java.util.List;
import java.util.Random;

import burlap.behavior.singleagent.Policy;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.GroundedSingleAction;

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
		
		
		GroundedSingleAction output = new GroundedSingleAction(worldAgentName, domain.getSingleAction(ga.actionName()), ga.params);
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
