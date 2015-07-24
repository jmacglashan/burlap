package burlap.behavior.policy;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;


/**
 * If you have a policy defined for one domain, and want to map it to use actions in another domain that have
 * the same name, you can use
 * this policy to perform the mapping. It takes the input source policy and a the target domain that
 * contains the actions to which the source policy action selections should be mapped.
 * @author James MacGlashan
 *
 */
public class DomainMappedPolicy extends Policy {

	/**
	 * The target domain containing the target actions that need to be selected.
	 */
	protected Domain targetDomain;
	
	/**
	 * The source policy that will be mapped into a the target domain's actions.
	 */
	protected Policy sourcePolicy;
	
	
	/**
	 * Initializes.
	 * @param targetDomain the domain to which actions in the source policy should be mapped
	 * @param sourcePolicy the source policy that selects actions
	 */
	public DomainMappedPolicy(Domain targetDomain, Policy sourcePolicy){
		this.targetDomain = targetDomain;
		this.sourcePolicy = sourcePolicy;
	}
	
	
	@Override
	public AbstractGroundedAction getAction(State s) {
		return this.mapAction(this.sourcePolicy.getAction(s));
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		List <ActionProb> aps = this.sourcePolicy.getActionDistributionForState(s);
		List <ActionProb> mapped = new ArrayList<Policy.ActionProb>(aps.size());
		for(ActionProb ap : aps){
			mapped.add(new ActionProb(this.mapAction(ap.ga), ap.pSelection));
		}
		
		return aps;
	}

	@Override
	public boolean isStochastic() {
		return this.sourcePolicy.isStochastic();
	}

	@Override
	public boolean isDefinedFor(State s) {
		return this.sourcePolicy.isDefinedFor(s);
	}
	
	
	/**
	 * Maps an input GroundedAction to a GroundedAction using an action reference of the action in this object's {@link #targetDomain} object that has the same name as the action in the input GroundedAction.
	 * @param ga the input GroundedAction to map.
	 * @return a GroundedAction whose action reference belongs to the Action with the same name in this object's {@link #targetDomain} object
	 */
	protected AbstractGroundedAction mapAction(AbstractGroundedAction ga){
		return new GroundedAction(targetDomain.getAction(ga.actionName()), ga.params);
	}

}
