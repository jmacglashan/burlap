package burlap.oomdp.stochasticgames;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.State;


/**
 * A single action is an action specification for individual agents in a stochastic game. The single action
 * specifies preconditions and any parameters that are required for it to operate. Like The 
 * {@link burlap.oomdp.singleagent.Action} class and {@link burlap.oomdp.core.PropositionalFunction} class,
 * SingleAction's can also specify parameter order groups for parameter effect symmetry. See the 
 * {@link burlap.oomdp.core.PropositionalFunction} class documentation for a larger discussion on
 * parameter order groups.
 * 
 * Unlike The {@link burlap.oomdp.singleagent.Action} class, SingleAction's do not specify
 * the actual transition dynamics, because transition dynamics in a stochastic game depend on
 * the full joint action of all agents (that is, the SingleAction selected by each agent in the world).
 * @author James MacGlashan
 *
 */
public abstract class SingleAction {

	public String actionName;
	public String [] parameterTypes;
	public String [] parameterOrderGroups;
	
	public SGDomain domain;
	
	
	/**
	 * Returns true if this action can be applied in the given state by the given agent with the given parameters.
	 * @param s the state in which the action would be executed.
	 * @param actingAgent the agent who would be executing the action
	 * @param params the parameters applied
	 * @return true if this action can be executed; false otherwise.
	 */
	public abstract boolean isApplicableInState(State s, String actingAgent, String [] params);
	
	
	/**
	 * Initializes this single action to be for the given domain and with the given name. This action
	 * is automatically added to the given domain
	 * @param d the domain to which this action belongs
	 * @param name the name of this action
	 */
	public SingleAction(SGDomain d, String name){
		this.init(d, name, new String[0], new String[0]);
	}
	
	
	/**
	 * Initializes this single action to be for the given domain, with the given name, and with
	 * the given parameter class types. This action
	 * is automatically added to the given domain
	 * @param d the domain to which this action belongs
	 * @param name the name of this action
	 * @param parameterTypes the object class names for the possible parameters of this action.
	 */
	public SingleAction(SGDomain d, String name, String [] parameterTypes){
		String [] pr = new String[parameterTypes.length];
		for(int i = 0; i < pr.length; i++){
			pr[i] = name + ".P" + i;
		}
		this.init(d, name, parameterTypes, pr);
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
	public SingleAction(SGDomain d, String name, String [] types, String [] parameterOrderGroups){
		this.init(d, name, types, parameterOrderGroups);
	}
	
	
	private void init(SGDomain d, String name, String [] pt, String [] pr){
		this.actionName = name;
		this.parameterTypes = pt;
		this.parameterOrderGroups = pr;
		d.addSingleAction(this);
		this.domain = d;
	}
	
	/**
	 * Returns true if this action is parameterized.
	 * @return true if this action is parameterized; false otherwise.
	 */
	public boolean isPamaeterized(){
		return this.parameterTypes.length > 0;
	}
	
	
	/**
	 * Returns true if all parameters (if any) for this action represent OO-MDP objects in a state; false otherwise.
	 * The default behavior is to return True; but this can be overriden for special actions.
	 * @return true if all parameters (if any) for this action represent OO-MDP objects in a state; false otherwise.
	 */
	public boolean parametersAreObjects(){
		return true;
	}
	
	/**
	 * Returns all possible grounded versions of the provided list of SingleAction objects that an agent can take in the given state. 
	 * @param s the state in which to execute actions
	 * @param actingAgent the agent who will be executing the actions
	 * @param actions the list of actions that to get the grounded version for
	 * @return all possible grounded versions of the provided list of SingleAction objects
	 */
	public static List <GroundedSingleAction> getAllPossibleGroundedSingleActions(State s, String actingAgent, List <SingleAction> actions){
		List <GroundedSingleAction> res = new ArrayList<GroundedSingleAction>();
		for(SingleAction sa : actions){
			res.addAll(sa.getAllGroundedActionsFor(s, actingAgent));
		}
		return res;
	}
	
	
	/**
	 * Returns all possible grounded versions of this single action for a given state and acting agent.
	 * @param s the state in which the agent would execute this action
	 * @param actingAgent the agent who would execute the action
	 * @return all possible grounded versions of this single action for a given state and acting agent.
	 */
	public List<GroundedSingleAction> getAllGroundedActionsFor(State s, String actingAgent){
		
		List <GroundedSingleAction> res = new ArrayList<GroundedSingleAction>();
		
		if(this.parameterTypes.length == 0){
			if(this.isApplicableInState(s, actingAgent, new String[]{})){
				res.add(new GroundedSingleAction(actingAgent, this, new String[]{}));
			}
			return res; //no parameters so just the single ga without params
		}
		
		List <List <String>> bindings = s.getPossibleBindingsGivenParamOrderGroups(this.parameterTypes, this.parameterOrderGroups);
		
		for(List <String> params : bindings){
			String [] aparams = params.toArray(new String[params.size()]);
			if(this.isApplicableInState(s, actingAgent, aparams)){
				res.add(new GroundedSingleAction(actingAgent, this, aparams));
			}
		}
		
		
		return res;
		
	}
	
	
	@Override
	public int hashCode(){
		return actionName.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		if(!(o instanceof SingleAction)){
			return false;
		}
		
		return ((SingleAction)o).actionName.equals(actionName);
	}

}
