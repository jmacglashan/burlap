package burlap.oomdp.stochasticgames.common;

import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.SGDomain;
import burlap.oomdp.stochasticgames.SingleAction;


/**
 * This {@link burlap.oomdp.stochasticgames.SingleAction} definition defines an action with a given name and parameters that can be
 * executed in every state by every agent. This is a useful action definition for symmetric stage games.
 * @author James MacGlashan
 *
 */
public class UniversalSingleAction extends SingleAction {

	
	
	/**
	 * Initializes this single action to be for the given domain and with the given name. This action
	 * is automatically added to the given domain
	 * @param d the domain to which this action belongs
	 * @param name the name of this action
	 */
	public UniversalSingleAction(SGDomain d, String name) {
		super(d, name);
	}
	
	
	/**
	 * Initializes this single action to be for the given domain, with the given name, and with
	 * the given parameter class types. This action
	 * is automatically added to the given domain
	 * @param d the domain to which this action belongs
	 * @param name the name of this action
	 * @param types the object class names for the possible parameters of this action.
	 */
	public UniversalSingleAction(SGDomain d, String name, String [] types){
		super(d, name, types);
	}
	
	
	/**
	 * Initializes this single action to be for the given domain, with the given name,  with
	 * the given parameter class types, and with the given parameter order groups. This action
	 * is automatically added to the given domain
	 * @param d the domain to which this action belongs
	 * @param name the name of this action
	 * @param types the object class names for the possible parameters of this action.
	 * @param parameterOrderGroups the parameter order groups to use
	 */
	public UniversalSingleAction(SGDomain d, String name, String [] types, String [] parameterOrderGroups){
		super(d, name, types, parameterOrderGroups);
	}

	@Override
	public boolean isApplicableInState(State s, String actingAgent, String [] params) {
		return true;
	}

}
