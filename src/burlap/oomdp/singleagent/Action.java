package burlap.oomdp.singleagent;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TransitionProbability;


/**
 * Abstract class for defining what happens when an action is executed in a state. The method {@link #getTransitions(burlap.oomdp.core.State, String[])}
 * is what defines the transition dynamics of the MDP for this action. If this method is not overridden by subclasses, then an
 * UnsupportedOperation exception is thrown . If the domain being created is only
 * going to be used planning/learning algorithms that require a generative model, rather than the fully enumerated transition
 * dynamics, then the {@link #getTransitions(burlap.oomdp.core.State, String[])} does not need to be implemented, but for full robustness it should be.
 * If your domain is deterministic, you can trivially implement it by having it return a call to the {@link #deterministicTransition(burlap.oomdp.core.State, String[])}
 * method, which will wrap the result of a {@link #performAction(burlap.oomdp.core.State, String[])}} method with a 1.0 outcome probability
 * {@link burlap.oomdp.core.TransitionProbability} object and insert it in a list containing just that element.
 * <p/>
 * Action objects may also be defined to require object parameters (which must adhere to a type). Parameters can also have parameter order groups specified if
 * there is effect symmetry when changing the order of the parameters. That is, if you swapped the parameter assignments for parameters in the same order group, the action would have
 * the same effect. However, if you swapped the parameter assignments of two parameters in different order groups, the action would have a different effect. 
 * For more information on parameter order groups, see its discussion
 * in the {@link burlap.oomdp.core.PropositionalFunction} class description.
 * @author James MacGlashan
 *
 */
public abstract class Action {

	/**
	 * The name of the action that can uniquely identify it
	 */
	protected String					name;									
	
	/**
	 * The domain with which this action is associated
	 */
	protected Domain					domain;
	
	/**
	 * The object classes each parameter of this action can accept; empty list for a parameter-less action (which is the default)
	 */
	protected String []					parameterClasses = new String[0];
	
	/**
	 * Specifies the parameter order group each parameter. Parameters in the same order group are order invariant; that is, if you swapped the parameter assignments for for parameters in the same group, the action would have
	 * the same effect. However, if you swapped the parameter assignments of two parameters in different order groups, the action would have a different effect.
	 */
	protected String []					parameterOrderGroup = new String[0];
	
	
	/**
	 * An observer that will be notified of an actions results every time it is executed. By default no observer is specified.
	 */
	protected List<ActionObserver>		actionObservers = new ArrayList<ActionObserver>();
	
	
	public Action(){
		//should not be called directly, but may be useful for subclasses of Action
	}
	
	
	/**
	 * Initializes the action with the name of the action, the domain to which it belongs, and the parameters it takes.
	 * The action will also be automatically be added to the domain. The parameter order group is set to be a unique order
	 * group for each parameter.
	 * @param name the name of the action
	 * @param domain the domain to which the action belongs
	 * @param parameterClasses a comma delineated String of the names of the object classes to which bound parameters must belong 
	 */
	public Action(String name, Domain domain, String parameterClasses){
		
		String [] pClassArray;
		if(parameterClasses.equals("")){
			pClassArray = new String[0];
		}
		else{
			pClassArray = parameterClasses.split(",");
		}
		
		//without parameter order group specified, all parameters are assumed to be in a different group
		String [] pog = new String[pClassArray.length];
		for(int i = 0; i < pog.length; i++){
			pog[i] = name + ".P" + i;
		}
		
		this.init(name, domain, pClassArray, pog);
		
	}
	
	
	/**
	 * Initializes the action with the name of the action, the domain to which it belongs, and the parameters it takes.
	 * The action will also be automatically be added to the domain. The parameter order group is set to be a unique order
	 * group for each parameter.
	 * @param name the name of the action
	 * @param domain the domain to which the action belongs
	 * @param parameterClasses a String array of the names of the object classes to which bound parameters must belong 
	 */
	public Action(String name, Domain domain, String [] parameterClasses){
		
		String [] pog = new String[parameterClasses.length];
		//without parameter order group specified, all parameters are assumed to be in a different group
		for(int i = 0; i < pog.length; i++){
			pog[i] = name + ".P" + i;
		}
		this.init(name, domain, parameterClasses, pog);
		
	}
	
	
	/**
	 * Initializes the action with the name of the action, the domain to which it belongs, the parameters it takes, and the parameter order groups.
	 * The action will also be automatically be added to the domain.
	 * @param name the name of the action
	 * @param domain the domain to which the action belongs
	 * @param parameterClasses a String array of the names of the object classes to which bound parameters must belong 
	 * @param parameterOrderGroups the order group assignments for each of the parameters.
	 */
	public Action(String name, Domain domain, String [] parameterClasses, String [] parameterOrderGroups){
		this.init(name, domain, parameterClasses, parameterOrderGroups);
	}
	
	
	protected void init(String name, Domain domain, String [] parameterClasses, String [] parameterOrderGroups){
		
		this.name = name;
		this.domain = domain;
		this.domain.addAction(this);
		this.parameterClasses = parameterClasses;
		this.parameterOrderGroup = parameterOrderGroups;
		
	}
	
	
	/**
	 * Returns the name of the action
	 * @return the name of the action
	 */
	public final String getName(){
		return name;
	}
	
	
	/**
	 * Returns a String array of the names of of the object classes to which bound parameters must belong
	 * @return a String array of the names of of the object classes to which bound parameters must belong. The array is empty if this action does not require parameters.
	 */
	public final String[] getParameterClasses(){
		return parameterClasses;
	}
	
	
	/**
	 * Returns the a String array specifying the parameter order group of each parameter.
	 * @return the a String array specifying the parameter order group of each parameter. The array is empty if this action does not require parameters.
	 */
	public final String[] getParameterOrderGroups(){
		return parameterOrderGroup;
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
	 * Returns the domain to which this action belongs.
	 * @return the domain to which this action belongs.
	 */
	public final Domain getDomain(){
		return domain;
	}
	
	/**
	 * Sets an action observer for this action. Set to null to specify no observer or to disable observaiton.
	 * @param observer the observer that will be told of each event when this action is executed.
	 */
	public void addActionObserver(ActionObserver observer){
		this.actionObservers.add(observer);
	}
	
	
	/**
	 * Clears all action observers associated with this action
	 */
	public void clearAllActionsObservers(){
		this.actionObservers.clear();
	}
	
	
	/**
	 * Returns true if this action can be applied in this specified state with the specified parameters.
	 * Default behavior is that an action can be applied in any state, but the {@link #applicableInState(State, String [])}
	 * method will need to be override if this is not the case.
	 * @param s the state in which to check if this action can be applied
	 * @param params a comma delineated String specifying the action object parameters
	 * @return true if this action can be applied in this specified state with the specified parameters; false otherwise.
	 */
	public final boolean applicableInState(State s, String params){
		return applicableInState(s, params.split(","));
	}
	
	/**
	 * Returns true if this action can be applied in this specified state with the specified parameters.
	 * Default behavior is that an action can be applied in any state,
	 * but this will need be overridden if that is not the case.
	 * @param s the state to perform the action on
	 * @param params a String array specifying the action object parameters
	 * @return whether the action can be performed on the given state
	 */
	public boolean applicableInState(State s, String [] params){

		return true; 
	}
	
	
	/**
	 * Performs this action in the specified state using the specified parameters and returns the resulting state. The input state
	 * will not be modified. The method will return a copy of the input state if the action is not applicable in state s with parameters params.
	 * @param s the state in which the action is to be performed.
	 * @param params a comma delineated String specifying the action object parameters
	 * @return the state that resulted from applying this action
	 */
	public final State performAction(State s, String params){
		return performAction(s, params.split(","));
		
	}
	
	
	/**
	 * Performs this action in the specified state using the specified parameters and returns the resulting state. The input state
	 * will not be modified with a deep copied state returned instead (unless this method is overriden, which may result in a semi-deep copy).
	 * If the action is not applicable in state s with parameters params, then a copy of the input state is returned.
	 * In general Action subclasses should *NOT* override this method and should instead override the abstract {@link #performActionHelper(State, String[])} method.
	 * Only override this method if you are seeking to perform memory optimization with semi-shallow copies of states and know what you're doing.
	 * @param s the state in which the action is to be performed.
	 * @param params a String array specifying the action object parameters
	 * @return the state that resulted from applying this action
	 */
	public State performAction(State s, String [] params){
		
		State resultState = s.copy();
		if(!this.applicableInState(s, params)){
			return resultState; //can't do anything if it's not applicable in the state so return the current state
		}
		
		resultState = performActionHelper(resultState, params);
		
		for(ActionObserver observer : this.actionObservers){
			observer.actionEvent(resultState, new GroundedAction(this, params), resultState);
		}
		
		return resultState;
		
	}
	
	
	/**
	 * Returns whether this action is a primitive action of the domain or not. A primitive action
	 * is defined to be an action that always takes one time step.
	 * @return true if the action is primitive; false otherwise.
	 */
	public boolean isPrimitive(){
		return true;
	}
	
	
	/**
	 * Returns the transition probabilities for applying this action in the given state with the given set of parameters.
	 * Transition probabilities are specified as list of {@link burlap.oomdp.core.TransitionProbability} objects. The list
	 * is only required to contain transitions with non-zero probability. By default, this method assumes that transition
	 * dynamics are deterministic and it returns a list with a single TransitionProbability with probability 1 whose
	 * state is determined by querying the {@link #performAction(State, String [])} method. If the transition dynamics
	 * are stochastic, then the analogous method {@link #getTransitions(State, String [])} needs to be overridden.
	 * @param s the state from which the transition probabilities when applying this action will be returned.
	 * @param params a comma delineated String specifying the action object parameters
	 * @return a List of transition probabilities for applying this action in the given state with the given set of parameters
	 */
	public final List<TransitionProbability> getTransitions(State s, String params){
		return this.getTransitions(s, params.split(","));
	}
	
	
	
	/**
	 * Returns the transition probabilities for applying this action in the given state with the given set of parameters.
	 * Transition probabilities are specified as list of {@link burlap.oomdp.core.TransitionProbability} objects. The list
	 * is only required to contain transitions with non-zero probability. Since not all planning algorithms require
	 * the full transition dynamics (and since it's impossible to enumerate them in some infinite state space domains),
	 * this method is not requried to be implemented. However, it will throw an UnsupportedOperationException
	 * if it is not overriden by the Action subclass if it is called by an algorithm that requires it.
	 * @param s the state from which the transition probabilities when applying this action will be returned.
	 * @param params a String array specifying the action object parameters
	 * @return a List of transition probabilities for applying this action in the given state with the given set of parameters
	 */
	public List<TransitionProbability> getTransitions(State s, String [] params){
		throw new UnsupportedOperationException("The full transition dynamics for action " + this.getName() + "  were" +
				"request, but have not be defined in the implemented Action class. Please override the " +
				"getTransitions(State String[] params) method for this action.");
	}


	/**
	 * Returns the transition dynamics by assuming the action to be deterministic and wrapping the result of a
	 * {@link #performAction(burlap.oomdp.core.State, String[])} method with a 1.0 probable {@link TransitionProbability}
	 * object and inserting it in the returned list.
	 * @param s the state from which the transition probabilities when applying this action will be returned.
	 * @param params a String array specifying the action object parameters
	 * @return a List of one element of type {@link burlap.oomdp.core.TransitionProbability} whose state is the outcome of the {@link #performAction(burlap.oomdp.core.State, String[])} method.
	 */
	protected List<TransitionProbability> deterministicTransition(State s, String [] params){
		List <TransitionProbability> transition = new ArrayList<TransitionProbability>();
		State res = this.performAction(s, params);
		transition.add(new TransitionProbability(res, 1.0));

		return transition;
	}
	
	
	/**
	 * Returns all possible groundings of this action that can be applied in the provided {@link State}. To check if a grounded
	 * action is applicable in the state, the {@link #applicableInState(State, String[])} method is checked.
	 * The default behavior of this method is to treat the parameters as possible object bindings, finding all bindings
	 * that satisfy the object class typing specified and then checking them against the {@link #applicableInState(State, String[])}
	 * method. However, this class can also be overridden to provide custom
	 * grounding behavior or non-object based parameterizations.
	 * @param s the {@link State} in which all applicable grounded actions of this {@link Action} object should be returned.
	 * @return a list of all applicable {@link GroundedAction}s of this {@link Action} object in in the given {@link State}
	 */
	public List<GroundedAction> getAllApplicableGroundedActions(State s){
		
		List <GroundedAction> res = new ArrayList<GroundedAction>();
		if(this.parameterClasses.length == 0){
			//parameterless action
			if(this.applicableInState(s, "")){
				res.add(new GroundedAction(this, ""));
			}
			return res; //no parameters to ground
		}
		
		//otherwise need to do parameter binding
		List <List <String>> bindings = s.getPossibleBindingsGivenParamOrderGroups(this.getParameterClasses(), this.getParameterOrderGroups());
		
		for(List <String> params : bindings){
			String [] aprams = params.toArray(new String[params.size()]);
			if(this.applicableInState(s, aprams)){
				GroundedAction gp = new GroundedAction(this, aprams);
				res.add(gp);
			}
		}
		
		return res;
	
	}
	
	
	/**
	 * Returns all {@link GroundedAction}s that are applicable in the given {@link State} for all {@link Action} objects in the provided list. This method
	 * operates by calling the {@link #getAllApplicableGroundedActions(State)} method on each action and adding all the results
	 * to a list that is then returned.
	 * @param actions The list of all actions for which grounded actions should be returned.
	 * @param s the state
	 * @return a {@link List} of all the {@link GroundedAction}s for all {@link Action} in the list that are applicable in the given {@link State}
	 */
	public static List<GroundedAction> getAllApplicableGroundedActionsFromActionList(List<Action> actions, State s){
		List<GroundedAction> res = new ArrayList<GroundedAction>();
		for(Action a : actions){
			res.addAll(a.getAllApplicableGroundedActions(s));
		}
		return res;
	}
	
	
	
	
	/**
	 * This method determines what happens when an action is applied in the given state with the given parameters. The State
	 * object s may be directly modified in this method since the parent method first copies the input state to pass
	 * to this helper method. The resulting state (which may be s) should then be returned.
	 * @param s the state to perform the action on
	 * @param params a String array specifying the action object parameters
	 * @return the resulting State from performing this action
	 */
	protected abstract State performActionHelper(State s, String [] params);
	
	
	
	@Override
	public boolean equals(Object obj){
		Action op = (Action)obj;
		if(op.name.equals(name))
			return true;
		return false;
	}
	
	
	@Override
	public int hashCode(){
		return name.hashCode();
	}
	
	
}
