package burlap.behavior.singleagent.learning.modellearning;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.singleagent.Policy;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;


/**
 * In model learning, it is not uncommon to have a modeled domain object with its own actions that are distinct from the actual action objects in the world.
 * If planning algorithm produces a policy for the modeled actions, the model policy needs to be mapped back into the actions of the real world. 
 * This class will take the real world domain
 * and the policy of the model domain and map its results into the actions of the real world domain.
 * @author James MacGlashan
 *
 */
public class DomainMappedPolicy extends Policy {

	/**
	 * The real world domain containing the real world actions that need to be executed.
	 */
	protected Domain			realWorldDomain;
	
	/**
	 * The policy formed over the model action space.
	 */
	protected Policy			modelPolicy;
	
	
	/**
	 * Initializes.
	 * @param realWorldDomain the domain to which actions in the model policy should be mapped
	 * @param modelPolicy the policy that selects modeled action objects
	 */
	public DomainMappedPolicy(Domain realWorldDomain, Policy modelPolicy){
		this.realWorldDomain = realWorldDomain;
		this.modelPolicy = modelPolicy;
	}
	
	
	@Override
	public AbstractGroundedAction getAction(State s) {
		return this.mapAction(this.modelPolicy.getAction(s));
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		List <ActionProb> aps = this.modelPolicy.getActionDistributionForState(s);
		List <ActionProb> mapped = new ArrayList<Policy.ActionProb>(aps.size());
		for(ActionProb ap : aps){
			mapped.add(new ActionProb(this.mapAction(ap.ga), ap.pSelection));
		}
		
		return aps;
	}

	@Override
	public boolean isStochastic() {
		return this.modelPolicy.isStochastic();
	}

	@Override
	public boolean isDefinedFor(State s) {
		return this.modelPolicy.isDefinedFor(s);
	}
	
	
	/**
	 * Maps an input GroundedAction to a GroundedAction using an action reference of the action in this object's {@link #realWorldDomain} object that has the same name as the action in the input GroundedAction.
	 * @param ga the input GroundedAction to map.
	 * @return a GroundedAction whose action reference belongs to the Action with the same name in this object's {@link #realWorldDomain} object 
	 */
	protected AbstractGroundedAction mapAction(AbstractGroundedAction ga){
		return new GroundedAction(realWorldDomain.getAction(ga.actionName()), ga.params);
	}

}
