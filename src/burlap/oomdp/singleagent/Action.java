package burlap.oomdp.singleagent;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TransitionProbability;


/**
 * Abstract class for defining what happens when an action is executed in a state. The method getTransitions(State s, String [] params)
 * is what defines the transition dynamics of the MDP for this action. If this method is not overridden by subclasses, then the default
 * behavior is to assume deterministic transition dynamics which are produced by sampling the performAction(State s, String [] params)
 * method and setting its returned state as having a transition probability of 1. If the domain being created is only
 * going to be used planning/learning algorithms that require a generative model, rather than the fully enumerated transition
 * dynamics, then the getTransitions(State s, String [] params) does not need to be defined, but for full robustness it should be.
 * 
 * Action objects may also be defined to be require object parameters which can also have parameter order groups specified if
 * there is effect symmetry when changing the order of the parameters. For more information on parameter order groups, see its discussion
 * in the {@link burlap.oomdp.core.PropositionalFunction} class description.
 * @author James MacGlashan
 *
 */
public abstract class Action {

	protected String					name;									//name of the action
	protected Domain					domain;									//domain that hosts the action
	protected String []					parameterClasses = new String[0];		//list of class names for each parameter of the action
	protected String []					parameterOrderGroup = new String[0];	//setting two or more parameters to the same order group indicates that the action will be same regardless of which specific object is set to each parameter
	
	
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
	 * Returns the domain to which this action belongs.
	 * @return the domain to which this action belongs.
	 */
	public final Domain getDomain(){
		return domain;
	}
	
	
	
	/**
	 * Returns true if this action can be applied in this specified state with the specified parameters.
	 * Default behavior is that an action can be applied in any state, but the {@link applicableInState(State, String [])}
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
	 * will not be modified. The method will return a copy of the input state if the action is not applicable in state s with parameters params.
	 * @param s the state in which the action is to be performed.
	 * @param params a String array specifying the action object parameters
	 * @return the state that resulted from applying this action
	 */
	public final State performAction(State s, String [] params){
		
		State resultState = s.copy();
		if(!this.applicableInState(s, params)){
			return resultState; //can't do anything if it's not applicable in the state so return the current state
		}
		
		return performActionHelper(resultState, params);
		
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
	 * state is determined by querying the {@link performAction(State, String [])} method. If the transition dynamics
	 * are stochastic, then the analogous method {@link getTransitions(State, String [])} needs to be overridden.
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
	 * is only required to contain transitions with non-zero probability. By default, this method assumes that transition
	 * dynamics are deterministic and it returns a list with a single TransitionProbability with probability 1 whose
	 * state is determined by querying the {@link performAction(State, String [])} method. If the transition dynamics
	 * are stochastic, then this method needs to be overridden.
	 * @param s the state from which the transition probabilities when applying this action will be returned.
	 * @param params a String array specifying the action object parameters
	 * @return a List of transition probabilities for applying this action in the given state with the given set of parameters
	 */
	public List<TransitionProbability> getTransitions(State s, String [] params){
		
		List <TransitionProbability> transition = new ArrayList<TransitionProbability>();
		State res = this.performAction(s, params);
		transition.add(new TransitionProbability(res, 1.0));
		
		return transition;
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
	
	
	
	
	public boolean equals(Object obj){
		Action op = (Action)obj;
		if(op.name.equals(name))
			return true;
		return false;
	}
	
	public int hashCode(){
		return name.hashCode();
	}
	
	
}
