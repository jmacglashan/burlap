package burlap.oomdp.singleagent;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;

import java.util.ArrayList;
import java.util.List;


/**
 * An abstract class for defining MDP action definitions. An {@link Action} definition includes a name for the action,
 * the preconditions for the action to be executable, and, potentially, the transition dynamics if the {@link burlap.oomdp.singleagent.Action} implementation
 * implements the {@link burlap.oomdp.singleagent.FullActionModel} interface.
 * <p>
 * An {@link burlap.oomdp.singleagent.Action} is closely associated with an implementation of the {@link burlap.oomdp.singleagent.GroundedAction}
 * class. A {@link burlap.oomdp.singleagent.GroundedAction} differs from an {@link burlap.oomdp.singleagent.Action} in
 * that it includes any parameter assignments necessary to execute the action that is provided to the appropriate
 * {@link burlap.oomdp.singleagent.Action} definition method.
 * <p>
 * Typically,
 * the name of the action along with
 * the {@link burlap.oomdp.core.Domain} with which this {@link burlap.oomdp.singleagent.Action} is to be associated are
 * specified in a constructor;
 * for example, {@link #Action(String, burlap.oomdp.core.Domain)}.
 * <p>
 * Defining an action requires implementing the following abstract methods.<p>
 * {@link #performActionHelper(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)},<p>
 * {@link #applicableInState(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)},<p>
 * {@link #isPrimitive},<p>
 * {@link #isParameterized()} <p>
 * {@link #getAssociatedGroundedAction()} and <p>
 * {@link #getAllApplicableGroundedActions(burlap.oomdp.core.states.State)}. <p>
 * The first thing to note about many of these methods is that a {@link burlap.oomdp.singleagent.GroundedAction} is provided
 * as a method argument. The provided {@link burlap.oomdp.singleagent.GroundedAction} is how an {@link burlap.oomdp.singleagent.Action}
 * implementation is told with which parameters it is being applied. If your action is is not parameterized, then this method argument
 * can be ignored.
 * <p>
 * The {@link #performActionHelper(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)} method should have the affect of sampling a transition from applying this {@link Action} in the input {@link State}
 * with the specified parameters and returning the sampled outcome. This method is always called indirectly by the {@link #performAction(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)}
 * method, which first makes a copy of the input state to be passed to {@link #performActionHelper(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)}.
 * Therefore, you can directly modify the input state of {@link #performActionHelper(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)} and return it if that is easiest.
 * This method will be used by planning
 * algorithms that use sampled transitions instead of enumerating the full transition dynamics or by deterministic planning
 * algorithms where there is not expected to ever be more than on possible outcome of an action. In general this method should always
 * be implemented. However, in some rare cases, it may not even be possible to define a model that can sample transitions
 * from arbitrary input states.
 * In such cases, it is okay to have this method throw a runtime exception instead of implementing it, but that means you
 * will only ever be able to use this action indirectly by applying it in an {@link burlap.oomdp.singleagent.environment.Environment},
 * which should know how to execute it (for example, by telling a robot to execute the action in the real world).
 * <p>
 * <p>
 * Implementing the {@link #applicableInState(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)} method is how preconditions can be specified.
 * If you do not override this method, then the default behavior is that the action will have no preconditions and can be applied
 * in any state. This method takes as input a {@link burlap.oomdp.core.states.State} and the parameters for this action (if any),
 * and returns true if the action can be applied in that state and false otherwise.
 * <p>
 * The {@link #isPrimitive()} method should usually return true and should only return false for special hierarchical actions like an {@link burlap.behavior.singleagent.options.Option}.
 * <p>
 * The other three methods are important for parameterized actions. If your action is not parameterized, consider subclassing {@link burlap.oomdp.singleagent.common.SimpleAction},
 * which is useful for defining non-parameterized primitive actions without preconditions, because it implements every abstract method except {@link #performActionHelper(burlap.oomdp.core.states.State, GroundedAction)}.
 * Otherwise these methods will need to be implemented to define the parameterization of your action.
 * <p>
 * If your action is parameterized, first, the {@link #isParameterized()} method should be overriden and set to return true. Next, as noted previously, an {@link burlap.oomdp.singleagent.GroundedAction} implementation
 * stores a set of parameter assignments that need to be provided to apply your parameterized {@link burlap.oomdp.singleagent.Action}.
 * Therefore, for custom parameterizations, you will need to subclass {@link burlap.oomdp.singleagent.GroundedAction} to include data
 * members for parameter assignments and the {@link #getAssociatedGroundedAction()} should return an instance of your custom
 * {@link burlap.oomdp.singleagent.GroundedAction} with its {@link burlap.oomdp.singleagent.GroundedAction#action} datamember
 * pointing to this {@link burlap.oomdp.singleagent.Action}. The parameter assignments in the returned {@link burlap.oomdp.singleagent.GroundedAction}
 * do not need to be specified; this method serves as a means for simply generating an instance of the associated {@link burlap.oomdp.singleagent.GroundedAction}.
 * <p>
 * The {@link #getAllApplicableGroundedActions(burlap.oomdp.core.states.State)} method should return a list of {@link burlap.oomdp.singleagent.GroundedAction}
 * instances that cover the space of all possible parameterizations of the action for in the input {@link burlap.oomdp.core.states.State}. However,
 * the returned list should only include {@link burlap.oomdp.singleagent.GroundedAction} instances that satisfy the
 * {@link #applicableInState(burlap.oomdp.core.states.State, GroundedAction)} method. Do *NOT* include {@link burlap.oomdp.singleagent.GroundedAction} objects
 * that are not applicable in the input list.
 * <p>
 * By allowing you to
 * define your own subclass of {@link burlap.oomdp.singleagent.GroundedAction} that is returned by these methods, you can have any kind of {@link burlap.oomdp.singleagent.Action}
 * parametrization that you'd like. That said, A common form of {@link burlap.oomdp.singleagent.Action} parameterization is an action that operates on OO-MDP
 * {@link burlap.oomdp.core.states.oo.ObjectInstance} references in a state (for example, stacking on block on another
 * in {@link burlap.domain.singleagent.blocksworld.BlocksWorld}. Therefore, if you would like to have a OO-MDP object parameterization,
 * rather than define your own subclass, you should consider subclassing the {@link burlap.oomdp.singleagent.ObjectParameterizedAction}
 * class. See it's documentation for more details.
 * <p>
 *
 * Also of note is the the {@link #performInEnvironment(burlap.oomdp.singleagent.environment.Environment, burlap.oomdp.singleagent.GroundedAction)} method.
 * This method handles having an action executed in some {@link burlap.oomdp.singleagent.environment.Environment} rather than simulated.
 * In general, this method does not
 * need to be overridden for the vast majority of cases (one exception is hierarchical actions like the {@link burlap.behavior.singleagent.options.Option} class, which
 * overrides it to have a sequence of primitive actions applied in the environment).
 * Typically, {@link burlap.behavior.singleagent.learning.LearningAgent}'s will execute actions in the {@link burlap.oomdp.singleagent.environment.Environment}
 * from which they're learning using this method.
 *
 *
 *
 * @author James MacGlashan
 *
 */
public abstract class Action{

	/**
	 * The name of the action that can uniquely identify it
	 */
	protected String					name;									
	
	/**
	 * The domain with which this action is associated
	 */
	protected Domain					domain;

	
	/**
	 * An observer that will be notified of an actions results every time it is executed. By default no observer is specified.
	 */
	protected List<ActionObserver>		actionObservers = new ArrayList<ActionObserver>();
	
	
	public Action(){
		//should not be called directly, but may be useful for subclasses of Action
	}


	public Action(String name, Domain domain){
		this.name = name;
		this.domain = domain;
		this.domain.addAction(this);
	}

	
	
	/**
	 * Returns the name of the action
	 * @return the name of the action
	 */
	public final String getName(){
		return name;
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
	 * Returns true if this action can be applied in this specified state with the parameters
	 * specified by the provided {@link burlap.oomdp.singleagent.GroundedAction}
	 * Default behavior is that an action can be applied in any state,
	 * but this will need be overridden if that is not the case.
	 * @param s the state to perform the action on
	 * @param groundedAction the {@link burlap.oomdp.singleagent.GroundedAction} specifying the parameters to use
	 * @return whether this action can be performed on the given state with the given parameters
	 */
	public abstract boolean applicableInState(State s, GroundedAction groundedAction);
	


	/**
	 * Executes this action with the specified parameters in the provided environment and returns the {@link burlap.oomdp.singleagent.environment.EnvironmentOutcome} result.
	 * @param env the environment in which the action should be performed.
	 * @param groundedAction the {@link burlap.oomdp.singleagent.GroundedAction} specifying the parameters to use
	 * @return an {@link burlap.oomdp.singleagent.environment.EnvironmentOutcome} specifying the result of the action execution in the environment
	 */
	public EnvironmentOutcome performInEnvironment(Environment env, GroundedAction groundedAction){
		return env.executeAction(groundedAction);
	}
	
	/**
	 * Performs this action in the specified state using the specified parameters and returns the resulting state. The input state
	 * will not be modified.
	 * If the action is not applicable in state s with parameters params, then a copy of the input state is returned.
	 * In general Action subclasses should *NOT* override this method and should instead override the abstract {@link #performActionHelper(State, burlap.oomdp.singleagent.GroundedAction)} method.
	 * Only override this method if you are seeking to perform memory optimization with semi-shallow copies of states and know what you're doing.
	 * @param s the state in which the action is to be performed.
	 * @param groundedAction the {@link burlap.oomdp.singleagent.GroundedAction} specifying the parameters to use
	 * @return the state that resulted from applying this action
	 */
	public State performAction(State s, GroundedAction groundedAction){
		
		State resultState = s.copy();
		if(!this.applicableInState(s, groundedAction)){
			return resultState; //can't do anything if it's not applicable in the state so return the current state
		}
		
		resultState = performActionHelper(resultState, groundedAction);
		
		for(ActionObserver observer : this.actionObservers){
			observer.actionEvent(resultState, groundedAction, resultState);
		}
		
		return resultState;
		
	}
	
	
	/**
	 * Returns whether this action is a primitive action of the domain or not. A primitive action
	 * is defined to be an action that always takes one time step.
	 * @return true if the action is primitive; false otherwise.
	 */
	public abstract boolean isPrimitive();


	/**
	 * Returns true if this action is parameterized; false otherwise.
	 * @return true if this {@link burlap.oomdp.singleagent.Action} is parameterized; false if it is not.
	 */
	public abstract boolean isParameterized();




	/**
	 * Returns the transition dynamics by assuming the action to be deterministic and wrapping the result of a
	 * {@link #performAction(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)} method with a 1.0 probable {@link TransitionProbability}
	 * object and inserting it in the returned list.
	 * @param s the state from which the transition probabilities when applying this action will be returned.
	 * @param groundedAction the {@link burlap.oomdp.singleagent.GroundedAction} specifying the parameters to use
	 * @return a List of one element of type {@link burlap.oomdp.core.TransitionProbability} whose state is the outcome of the {@link #performAction(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)} method.
	 */
	protected List<TransitionProbability> deterministicTransition(State s, GroundedAction groundedAction){
		List <TransitionProbability> transition = new ArrayList<TransitionProbability>();
		State res = this.performAction(s, groundedAction);
		transition.add(new TransitionProbability(res, 1.0));

		return transition;
	}



	/**
	 * Returns a {@link burlap.oomdp.singleagent.GroundedAction} instance that points to this {@link burlap.oomdp.singleagent.Action},
	 * but does not have any parameters--if any--set.
	 * @return a {@link burlap.oomdp.singleagent.GroundedAction} instance.
	 */
	public abstract GroundedAction getAssociatedGroundedAction();


	/**
	 * Returns the {@link GroundedAction} instance associated with this action with its parameters set to the provided
	 * string representation of the parameters. This method works by first calling an the {@link #getAssociatedGroundedAction()}
	 * method of this object, and then calling the {@link GroundedAction#initParamsWithStringRep(String[])} method
	 * to set its parameters with the string representations. Consequently, the provided {@link GroundedAction}
	 * implementation must implement the {@link GroundedAction#initParamsWithStringRep(String[])} for this method to work.
	 * @param strParams the parameters of the action specified with their string representation.
	 * @return a {@link burlap.oomdp.singleagent.GroundedAction} instance.
	 */
	public GroundedAction getGroundedAction(String...strParams){
		GroundedAction ga = this.getAssociatedGroundedAction();
		ga.initParamsWithStringRep(strParams);
		return ga;
	}

	/**
	 * Returns all possible groundings of this action that can be applied in the provided {@link State}. To check if a grounded
	 * action is applicable in the state, the {@link #applicableInState(State, burlap.oomdp.singleagent.GroundedAction)} method is checked.
	 * The default behavior of this method is to treat the parameters as possible object bindings, finding all bindings
	 * that satisfy the object class typing specified and then checking them against the {@link #applicableInState(State, burlap.oomdp.singleagent.GroundedAction)}
	 * method. However, this class can also be overridden to provide custom
	 * grounding behavior or non-object based parametrization.
	 * @param s the {@link State} in which all applicable grounded actions of this {@link Action} object should be returned.
	 * @return a list of all applicable {@link GroundedAction}s of this {@link Action} object in in the given {@link State}
	 */
	public abstract List<GroundedAction> getAllApplicableGroundedActions(State s);
	
	
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
	 * object s may be directly modified in this method since the parent method ({@link #performAction(burlap.oomdp.core.states.State, GroundedAction)}
	 * first copies the input state to pass
	 * to this helper method. The resulting state (which may be s) should then be returned.
	 * @param s the state to perform the action on
	 * @param groundedAction the {@link burlap.oomdp.singleagent.GroundedAction} specifying the parameters to use
	 * @return the resulting State from performing this action
	 */
	protected abstract State performActionHelper(State s, GroundedAction groundedAction);
	
	
	
	@Override
	public boolean equals(Object obj){
	    if (obj == null || this.getClass() != obj.getClass()) {
            return false;   
        }
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
