package burlap.behavior.stochasticgame.saconversion;

import java.util.Map;

import burlap.behavior.singleagent.Policy;
import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Domain;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.stochasticgames.AgentType;
import burlap.oomdp.stochasticgames.JointActionModel;
import burlap.oomdp.stochasticgames.SGDomain;
import burlap.oomdp.stochasticgames.SingleAction;

/**
 * convert a SGDomain to an SADomain
 * @author Betsy Hilliard betsy@cs.brown.edu
 *
 */

public class ConversionGenerator implements DomainGenerator{

	protected SGDomain sgDomain;
	protected JointActionModel jaModel;
	protected AgentType agentType;
	protected String agentName;
	protected Map<String, Policy> otherAgentPolicies;


	public ConversionGenerator(SGDomain sgDomain, JointActionModel jaModel, AgentType agentType, String agentName, 
			Map<String, Policy> otherAgentPolicies){

		this.sgDomain = sgDomain;
		this.jaModel = jaModel;
		this.agentType = agentType;
		this.agentName = agentName;
		this.otherAgentPolicies = otherAgentPolicies;

	}

	@Override
	public Domain generateDomain() {

		SADomain newDomain = new SADomain();
		System.out.println("sgDomain: "+sgDomain);
		for(SingleAction a : agentType.actions){

			new SGActionWrapper(a, jaModel, agentName, otherAgentPolicies, newDomain);

		}

		return newDomain;
	}


}
