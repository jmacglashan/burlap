package burlap.oomdp.singleagent;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.states.State;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;


/**
 * Abstract class for defining MDP action definitions. An {@link Action} definition includes a name for the action,
 * what kind of parameters it operates on (if any),
 * the preconditions for the action to be executable, and potentially the transition dynamics. Typically,
 * the name of th action and the parameter types on which it operates are defined in a constructor, along with
 * the {@link burlap.oomdp.core.Domain} with which this {@link burlap.oomdp.singleagent.Action} is to be associated;
 * for example, {@link #Action(String, burlap.oomdp.core.Domain, String[])}. If the {@link Action} you are defining
 * is not parametrized, then the params argument of the constructor can be set to an empty array. More information
 * on parameters can be found below.
 * {@link burlap.oomdp.core.ObjectClass}
 * <br/><br/>
 * <b>Important Methods</b><br/>
 * The most important methods to take note of are
 * {@link #performActionHelper(burlap.oomdp.core.states.State, String[])},
 * {@link #getTransitions(burlap.oomdp.core.states.State, String[])},
 * {@link #applicableInState(burlap.oomdp.core.states.State, String[])}, and
 * {@link #performInEnvironment(burlap.oomdp.singleagent.environment.Environment, String[])}. <br/>
 * Of these methods, only {@link #performActionHelper(burlap.oomdp.core.states.State, String[])} is required to be overridden and implemented.
 * This method should have the affect of sampling an transition from applying this {@link Action} in the input {@link State}
 * and returning the sampled outcome. This method is always called indirectly by the {@link #performAction(burlap.oomdp.core.states.State, String[])}
 * method, which first makes a copy of the input state before passing it {@link #performActionHelper(burlap.oomdp.core.states.State, String[])}.
 * Therefore, you can directly modify the input state and return it if that is easiest. This method will be used by planning
 * algorithms that use sampled transitions instead of enumerating the full transition dynamics or by deterministic planning
 * algorithms where there is not expected to ever be more than on possible outcome of an action. In general this method should always
 * be implemented. However, in some rare cases, such as robotics, it will not even be possible to define a model that can sample transitions
 * from arbitrary input states.
 * In such cases, it is okay to have this method throw a runtime exception instead of implementing it, but that means you
 * will only ever be able to use this action indirectly by asking an {@link burlap.oomdp.singleagent.environment.Environment}
 * to apply it, which should know how to execute it (for example, by telling a robot to execute the action in the real world).
 * <br/>
 * The {@link #getTransitions(burlap.oomdp.core.states.State, String[])} method provides the full transition dynamics of
 * an {@link burlap.oomdp.singleagent.Action}. Although this method does not have to overridden, if it is not
 * it will throw a runtime exception. This method is not required to be overridden because in many domains, the transition
 * dynamics are infinite or too large to enumerate, in which case only sampling (via the {@link #performAction(burlap.oomdp.core.states.State, String[])}
 * method) can be used. However, many planning algorithms, such as Dynamic programming methods, require the full transition dynamics,
 * so if you wish to use such an algorithm and it is possible to fully enumerate the transition dynamics, you should override and
 * implement this method. This method should return a list of all transitions from the input {@link burlap.oomdp.core.states.State}
 * that have non-zero probability of occurring. These transitions are specified with a {@link burlap.oomdp.core.TransitionProbability}
 * object that is a pair consisting of the next {@link burlap.oomdp.core.states.State} and a double specifying the probability
 * of transitioning to that state.
 * <br/>
 * Overriding the {@link #applicableInState(burlap.oomdp.core.states.State, String[])} method is how preconditions can be specified.
 * If you do not override this method, then the default behavior is that no actions have any preconditions and can be applied
 * in any state. This method takes as input a {@link burlap.oomdp.core.states.State} and the parameters for this action (if any),
 * and returns true if the action can be applied in that state and false otherwise.
 * <br/>
 * The {@link #performInEnvironment(burlap.oomdp.singleagent.environment.Environment, String[])} method does not
 * need to be overridden for the vast majority of case (the exception is hierarchical actions like {@link burlap.behavior.singleagent.options.Option})
 * This method allows an action to be executed in an {@link burlap.oomdp.singleagent.environment.Environment} in which the
 * outcomes may be different than this {@link burlap.oomdp.singleagent.Action}'s model of the world (as defined
 * by the {@link #performAction(burlap.oomdp.core.states.State, String[])} and {@link #getTransitions(burlap.oomdp.core.states.State, String[])} methods).
 * Typically, {@link burlap.behavior.singleagent.learning.LearningAgent}'s will execute actions in the {@link burlap.oomdp.singleagent.environment.Environment}
 * from which they're learning using this method.
 *
 * <br/<br/>
 * <b>Parameters</b><br/>
 * The default assumption for parameters (which can be relaxed, see more below)
 * is that any parameters of an {@link Action} are references to {@link burlap.oomdp.core.objects.ObjectInstance}s in a {@link burlap.oomdp.core.states.State}.
 * The string array in the constructor specifies the valid type of {@link burlap.oomdp.core.ObjectClass}
 * to which the parameters must belong. For example, in blocks world, we might define a "stack" action that takes two parameters
 * that each must be instances of the BLOCK class. In such a case, the String array passed to the constructor would be new String[]{"BLOCK", "BLOCK"}.
 * It may also be the case that the order of parameters is unimportant. For example, a cooking domain might have a "combine"
 * action that combines two INGREDIENT objects. In such a case, the effect of combine(ing1, ing2) would be the same as combine(ing2, ing1).
 * Our action definition can include this parameter symmetry information by assigning parameters to the same <i>parameter order group</i>. By default
 * the parameter order group of parameters are all assume to be different, which means the order of the parameters is important. However,
 * by using the {@link #Action(String, burlap.oomdp.core.Domain, String[], String[])} method, each parameter can also be set
 * to a parameter order group. For example, the parameterClasses of the combine action would be new String[]{INGREDIENT, INGREDIENT}, and
 * the parameterOrderGroups would be new String[]{g1, g1}, thereby placing them in the same group to indicate that their order
 * is unimportant.
 * <br/>
 * Parameters of an action do not have to be object references either. If you would like to specify your own kind
 * of parameters, you can override the {@link #getAllApplicableGroundedActions(burlap.oomdp.core.states.State)} method,
 * which should return the list of actions and their parameters (stored in a {@link burlap.oomdp.singleagent.GroundedAction}
 * instance) that can be applied in the input state. Additionally, you should then override the method {@link #parametersAreObjects()}
 * and have it return false.
 *
 *
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
	 * Executes this action with the specified parameters in the provided environment and returns the {@link burlap.oomdp.singleagent.environment.EnvironmentOutcome} result.
	 * @param env the environment in which the action should be performed.
	 * @param params String array specifying the action parameters
	 * @return an {@link burlap.oomdp.singleagent.environment.EnvironmentOutcome} specifying the result of the action execution in the environment
	 */
	public EnvironmentOutcome performInEnvironment(Environment env, String [] params){
		GroundedAction ga = new GroundedAction(this, params);
		return env.executeAction(ga);
	}
	
	/**
	 * Performs this action in the specified state using the specified parameters and returns the resulting state. The input state
	 * will not be modified with a deep copied state returned instead (unless this method is overriden, which may result in a semi-deep copy).
	 * If the action is not applicable in state s with parameters params, then a copy of the input state is returned.
	 * In general Action subclasses should *NOT* override this method and should instead override the abstract {@link #performActionHelper(State, String[])} method.
	 * Only override this method if you are seeking to perform memory optimization with semi-shallow copies of states and know what you're doing.
	 * @param s the state in which the action is to be performed.
	 * @param params a String array specifying the action parameters
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
	 * @param params a comma delineated String specifying the action parameters
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
	 * {@link #performAction(burlap.oomdp.core.states.State, String[])} method with a 1.0 probable {@link TransitionProbability}
	 * object and inserting it in the returned list.
	 * @param s the state from which the transition probabilities when applying this action will be returned.
	 * @param params a String array specifying the action object parameters
	 * @return a List of one element of type {@link burlap.oomdp.core.TransitionProbability} whose state is the outcome of the {@link #performAction(burlap.oomdp.core.states.State, String[])} method.
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
	 * grounding behavior or non-object based parametrization.
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
