package burlap.behavior.stochasticgames.saconversion;

import java.util.Map;

import burlap.behavior.policy.Policy;
import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Domain;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.statehashing.HashableStateFactory;
import burlap.oomdp.stochasticgames.JointActionModel;
import burlap.oomdp.stochasticgames.SGAgentType;
import burlap.oomdp.stochasticgames.SGDomain;
import burlap.oomdp.stochasticgames.agentactions.ObParamSGAgentAction;
import burlap.oomdp.stochasticgames.agentactions.SGAgentAction;

/**
 * convert a SGDomain to an SADomain
 * 
 * @author Betsy Hilliard betsy@cs.brown.edu
 *
 */

public class ConversionGenerator implements DomainGenerator {

	protected SGDomain sgDomain;
	protected JointActionModel jaModel;
	protected SGAgentType sgAgentType;
	protected String agentName;
	protected Map<String, Policy> otherAgentPolicies;
	HashableStateFactory hashFactory;

	public ConversionGenerator(SGDomain sgDomain, JointActionModel jaModel,
			SGAgentType sgAgentType, String agentName,
			Map<String, Policy> otherAgentPolicies, HashableStateFactory hashFactory) {

		this.sgDomain = sgDomain;
		this.jaModel = jaModel;
		this.sgAgentType = sgAgentType;
		this.agentName = agentName;
		this.otherAgentPolicies = otherAgentPolicies;
		this.hashFactory = hashFactory;

	}

	@Override
	public Domain generateDomain() {

		SADomain newDomain = new SADomain();
		// System.out.println("sgDomain: "+sgDomain);
		for (SGAgentAction a : sgAgentType.actions) {
			ObParamSGAgentAction paramAction = (ObParamSGAgentAction)a;
			new SGActionWrapper(paramAction, jaModel, agentName, otherAgentPolicies,
					newDomain, hashFactory, sgAgentType.actions, sgDomain);
//			new SGActionWrapper(a, jaModel, agentName, otherAgentPolicies,
//					newDomain, hashFactory, SGAgentType.actions);

		}

		return newDomain;
	}

}
