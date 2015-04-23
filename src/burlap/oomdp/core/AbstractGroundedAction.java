package burlap.oomdp.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.stochasticgames.GroundedSingleAction;
import burlap.oomdp.stochasticgames.JointAction;
import burlap.oomdp.stochasticgames.SingleAction;

/**
 * This is an abstract superclass for grounded actions. A grounded action is a refernce to an action along with the specific parameters with which the action
 * is to be applied. Subclasses for this class include the single agent action grounding ({@link GroundedAction}), an action grounding for a specific agent
 * in a stochastic game {@link GroundedSingleAction}, and a joint action in a stochastic game ({@link JointAction}).
 * @author James MacGlashan
 *
 */
public abstract class AbstractGroundedAction {

	/**
	 * Object parameters of the action (if any)
	 */
	public String [] params = new String[]{};
	
	
	/**
	 * Returns the action name for this grounded action.
	 * @return the action name for this grounded action.
	 */
	public abstract String actionName();
	
	
	/**
	 * Returns true if this grounded action can be directly executed on a state with the {@link #executeIn(State)} method. For example, a single agent domain grounded action is executable; a stochastic games single grounded action
	 * is not, because the action of all other agents must also be known in order to get the next state.
	 * @return true if this grounded action can be directly executed on a state; false otherwise.
	 */
	public abstract boolean isExecutable();
	
	
	/**
	 * Executes the grounded action on a given state
	 * @param s the state on which to execute the action
	 * @return The state after the action has been executed
	 */
	public abstract State executeIn(State s);
	
	
	
	/**
	 * Returns true if the domain to which this action belongs is object identifier dependent
	 * @return true if the domain to which this action belongs is object identifier dependent; false otherwise
	 */
	public abstract boolean actionDomainIsObjectIdentifierDependent();
	
	
	/**
	 * Returns a copy of this grounded action.
	 * @return a copy of this grounded action.
	 */
	public abstract AbstractGroundedAction copy();
	
	
	/**
	 * Returns true if this action uses parameters
	 * @return true if this action uses parameters; false otherwise
	 */
	public boolean isParameterized(){
		return this.params.length > 0;
	}
	
	
	/**
	 * Returns true if all parameters (if any) for this action represent OO-MDP objects in a state; false otherwise.
	 * This method will query the refenced action object to evaluate. (e.g., {@link GroundedAction} will query
	 * its referenced {@link Action} object; {@link GroundedSingleAction} will query its referenced {@link SingleAction}.
	 * @return true if all parameters (if any) for this action represent OO-MDP objects in a state; false otherwise.
	 */
	public abstract boolean parametersAreObjects();
	
	
	/**
	 * This method will translate this object's parameters that were assigned for a given source state, into object parameters in the
	 * target state that are equal. This method is useful if a domain uses parameterized actions and is object identifier invariant.
	 * If the domain of this grounded aciton's action is object identifier dependent, then no translation will occur
	 * and this object will be returned. This object will also be returned if it is a parameterless action.
	 * @param sourceState the source state from which this objects parameters were bound.
	 * @param targetState a target state with potentially different object identifiers for equivalent values.
	 * @return a grounded action object whose parameters have been translated to the target state object identifiers
	 */
	public AbstractGroundedAction translateParameters(State sourceState, State targetState){
		
		if(this.params.length == 0 || this.actionDomainIsObjectIdentifierDependent() || !this.parametersAreObjects()){
			//no need to translate a parameterless action or an action that belongs to a name dependent domain or actions that do not have objects as parameters
			return this;
		}
		
		AbstractGroundedAction aga = this.copy();
		
		Set <String> matchedObjects = new HashSet<String>();
		String [] nparams = new String[this.params.length];
		int i = 0;
		for(String oname : this.params){
			ObjectInstance o = sourceState.getObject(oname);
			List<ObjectInstance> cands = targetState.getObjectsOfClass(o.getObjectClass().name);
			for(ObjectInstance cand : cands){
				if(matchedObjects.contains(cand.getName())){
					continue ;
				}
				if(o.valueEquals(cand)){
					nparams[i] = o.getName();
					matchedObjects.add(o.getName());
					break;
				}
			}
			
			i++;
		}
		
		aga.params = nparams;
		
		return aga;
	}
	
}
