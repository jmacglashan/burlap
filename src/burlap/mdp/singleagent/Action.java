package burlap.mdp.singleagent;

import burlap.mdp.core.Domain;
import burlap.mdp.core.TransitionProbability;
import burlap.mdp.core.state.State;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import burlap.mdp.singleagent.oo.ObjectParameterizedAction;

import java.util.ArrayList;
import java.util.List;


/**
 * An abstract class for defining MDP action definitions. An {@link Action} definition includes a name for the action,
 * the preconditions for the action to be executable, and, potentially, the transition dynamics if the {@link burlap.mdp.singleagent.Action} implementation
 * implements the {@link burlap.mdp.singleagent.FullActionModel} interface.
 * <p>
 * An {@link burlap.mdp.singleagent.Action} is closely associated with an implementation of the {@link burlap.mdp.singleagent.GroundedAction}
 * class. A {@link burlap.mdp.singleagent.GroundedAction} differs from an {@link burlap.mdp.singleagent.Action} in
 * that it includes any parameter assignments necessary to execute the action that is provided to the appropriate
 * {@link burlap.mdp.singleagent.Action} definition method.
 * <p>
 * Typically,
 * the name of the action along with
 * the {@link burlap.mdp.core.Domain} with which this {@link burlap.mdp.singleagent.Action} is to be associated are
 * specified in a constructor;
 * for example, {@link #Action(String, burlap.mdp.core.Domain)}.
 * <p>
 * Defining an action requires implementing the following abstract methods.<p>
 * {@link #sampleHelper(State, burlap.mdp.singleagent.GroundedAction)},<p>
 * {@link #applicableInState(State, burlap.mdp.singleagent.GroundedAction)},<p>
 * {@link #isPrimitive},<p>
 * {@link #isParameterized()} <p>
 * {@link #associatedGroundedAction()} and <p>
 * {@link #allApplicableGroundedActions(State)}. <p>
 * The first thing to note about many of these methods is that a {@link burlap.mdp.singleagent.GroundedAction} is provided
 * as a method argument. The provided {@link burlap.mdp.singleagent.GroundedAction} is how an {@link burlap.mdp.singleagent.Action}
 * implementation is told with which parameters it is being applied. If your action is is not parameterized, then this method argument
 * can be ignored.
 * <p>
 * The {@link #sampleHelper(State, burlap.mdp.singleagent.GroundedAction)} method should have the affect of sampling a transition from applying this {@link Action} in the input {@link State}
 * with the specified parameters and returning the sampled outcome. This method is always called indirectly by the {@link #sample(State, burlap.mdp.singleagent.GroundedAction)}
 * method, which first makes a copy of the input state to be passed to {@link #sampleHelper(State, burlap.mdp.singleagent.GroundedAction)}.
 * Therefore, you can directly modify the input state of {@link #sampleHelper(State, burlap.mdp.singleagent.GroundedAction)} and return it if that is easiest.
 * This method will be used by planning
 * algorithms that use sampled transitions instead of enumerating the full transition dynamics or by deterministic planning
 * algorithms where there is not expected to ever be more than on possible outcome of an action. In general this method should always
 * be implemented. However, in some rare cases, it may not even be possible to define a model that can sample transitions
 * from arbitrary input states.
 * In such cases, it is okay to have this method throw a runtime exception instead of implementing it, but that means you
 * will only ever be able to use this action indirectly by applying it in an {@link burlap.mdp.singleagent.environment.Environment},
 * which should know how to execute it (for example, by telling a robot to execute the action in the real world).
 * <p>
 * Implementing the {@link #applicableInState(State, burlap.mdp.singleagent.GroundedAction)} method is how preconditions can be specified.
 * If you do not override this method, then the default behavior is that the action will have no preconditions and can be applied
 * in any state. This method takes as input a {@link State} and the parameters for this action (if any),
 * and returns true if the action can be applied in that state and false otherwise.
 * <p>
 * The {@link #isPrimitive()} method should usually return true and should only return false for special hierarchical actions like an {@link burlap.behavior.singleagent.options.Option}.
 * <p>
 * The other three methods are important for parameterized actions. If your action is not parameterized, consider subclassing {@link burlap.mdp.singleagent.common.SimpleAction},
 * which is useful for defining non-parameterized primitive actions without preconditions, because it implements every abstract method except {@link #sampleHelper(State, GroundedAction)}.
 * Otherwise these methods will need to be implemented to define the parameterization of your action.
 * <p>
 * If your action is parameterized, first, the {@link #isParameterized()} method should be overriden and set to return true. Next, as noted previously, an {@link burlap.mdp.singleagent.GroundedAction} implementation
 * stores a set of parameter assignments that need to be provided to apply your parameterized {@link burlap.mdp.singleagent.Action}.
 * Therefore, for custom parameterizations, you will need to subclass {@link burlap.mdp.singleagent.GroundedAction} to include data
 * members for parameter assignments and the {@link #associatedGroundedAction()} should return an instance of your custom
 * {@link burlap.mdp.singleagent.GroundedAction} with its {@link burlap.mdp.singleagent.GroundedAction#action} datamember
 * pointing to this {@link burlap.mdp.singleagent.Action}. The parameter assignments in the returned {@link burlap.mdp.singleagent.GroundedAction}
 * do not need to be specified; this method serves as a means for simply generating an instance of the associated {@link burlap.mdp.singleagent.GroundedAction}.
 * <p>
 * The {@link #allApplicableGroundedActions(State)} method should return a list of {@link burlap.mdp.singleagent.GroundedAction}
 * instances that cover the space of all possible parameterizations of the action for in the input {@link State}. However,
 * the returned list should only include {@link burlap.mdp.singleagent.GroundedAction} instances that satisfy the
 * {@link #applicableInState(State, GroundedAction)} method. Do *NOT* include {@link burlap.mdp.singleagent.GroundedAction} objects
 * that are not applicable in the input list.
 * <p>
 * By allowing you to
 * define your own subclass of {@link burlap.mdp.singleagent.GroundedAction} that is returned by these methods, you can have any kind of {@link burlap.mdp.singleagent.Action}
 * parametrization that you'd like. That said, A common form of {@link burlap.mdp.singleagent.Action} parameterization is an action that operates on OO-MDP
 * {@link ObjectInstance} references in a state (for example, stacking on block on another
 * in {@link burlap.domain.singleagent.blocksworld.BlocksWorld}. Therefore, if you would like to have a OO-MDP object parameterization,
 * rather than define your own subclass, you should consider subclassing the {@link ObjectParameterizedAction}
 * class. See it's documentation for more details.
 * <p>
 *
 * Also of note is the the {@link #executeIn(burlap.mdp.singleagent.environment.Environment, burlap.mdp.singleagent.GroundedAction)} method.
 * This method handles having an action executed in some {@link burlap.mdp.singleagent.environment.Environment} rather than simulated.
 * In general, this method does not
 * need to be overridden for the vast majority of cases (one exception is hierarchical actions like the {@link burlap.behavior.singleagent.options.Option} class, which
 * overrides it to have a sequence of primitive actions applied in the environment).
 * Typically, {@link burlap.behavior.singleagent.learning.LearningAgent}'s will execute actions in the {@link burlap.mdp.singleagent.environment.Environment}
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
	 * An observer that will be notified of an actions results every time it is executed. By default no observer is specified.
	 */
	protected List<ActionObserver>		actionObservers = new ArrayList<ActionObserver>();
	
	
	public Action(){
		//should not be called directly, but may be useful for subclasses of Action
	}


	public Action(String name, Domain domain){
		this.name = name;
		domain.addAction(this);
	}

	
	
	/**
	 * Returns the name of the action
	 * @return the name of the action
	 */
	public final String getName(){
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
	 * specified by the provided {@link burlap.mdp.singleagent.GroundedAction}
	 * Default behavior is that an action can be applied in any state,
	 * but this will need be overridden if that is not the case.
	 * @param s the state to perform the action on
	 * @param groundedAction the {@link burlap.mdp.singleagent.GroundedAction} specifying the parameters to use
	 * @return whether this action can be performed on the given state with the given parameters
	 */
	public abstract boolean applicableInState(State s, GroundedAction groundedAction);
	


	/**
	 * Executes this action with the specified parameters in the provided environment and returns the {@link burlap.mdp.singleagent.environment.EnvironmentOutcome} result.
	 * @param env the environment in which the action should be performed.
	 * @param groundedAction the {@link burlap.mdp.singleagent.GroundedAction} specifying the parameters to use
	 * @return an {@link burlap.mdp.singleagent.environment.EnvironmentOutcome} specifying the result of the action execution in the environment
	 */
	public EnvironmentOutcome executeIn(Environment env, GroundedAction groundedAction){
		return env.executeAction(groundedAction);
	}
	
	/**
	 * Samples a transition from in the specified state using the specified parameters and returns the resulting state. The input state
	 * will not be modified.
	 * If the action is not applicable in state s with parameters params, then a copy of the input state is returned.
	 * In general Action subclasses should *NOT* override this method and should instead override the abstract {@link #sampleHelper(State, burlap.mdp.singleagent.GroundedAction)} method.
	 * Only override this method if you are seeking to perform memory optimization with semi-shallow copies of states and know what you're doing.
	 * @param s the state from which an action transition is sampled
	 * @param groundedAction the {@link burlap.mdp.singleagent.GroundedAction} specifying the parameters to use
	 * @return a sampled {@link State} from the transition dynamics
	 */
	public State sample(State s, GroundedAction groundedAction){
		
		State resultState = s.copy();
		if(!this.applicableInState(s, groundedAction)){
			return resultState; //can't do anything if it's not applicable in the state so return the current state
		}
		
		resultState = sampleHelper(resultState, groundedAction);
		
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
	 * @return true if this {@link burlap.mdp.singleagent.Action} is parameterized; false if it is not.
	 */
	public abstract boolean isParameterized();




	/**
	 * Returns the transition dynamics by assuming the action to be deterministic and wrapping the result of a
	 * {@link #sample(State, burlap.mdp.singleagent.GroundedAction)} method with a 1.0 probable {@link TransitionProbability}
	 * object and inserting it in the returned list.
	 * @param s the state from which the transition probabilities when applying this action will be returned.
	 * @param groundedAction the {@link burlap.mdp.singleagent.GroundedAction} specifying the parameters to use
	 * @return a List of one element of type {@link burlap.mdp.core.TransitionProbability} whose state is the outcome of the {@link #sample(State, burlap.mdp.singleagent.GroundedAction)} method.
	 */
	protected List<TransitionProbability> deterministicTransition(State s, GroundedAction groundedAction){
		List <TransitionProbability> transition = new ArrayList<TransitionProbability>();
		State res = this.sample(s, groundedAction);
		transition.add(new TransitionProbability(res, 1.0));

		return transition;
	}



	/**
	 * Returns a {@link burlap.mdp.singleagent.GroundedAction} instance that points to this {@link burlap.mdp.singleagent.Action},
	 * but does not have any parameters--if any--set.
	 * @return a {@link burlap.mdp.singleagent.GroundedAction} instance.
	 */
	public abstract GroundedAction associatedGroundedAction();


	/**
	 * Returns the {@link GroundedAction} instance associated with this action with its parameters set to the provided
	 * string representation of the parameters. This method works by first calling an the {@link #associatedGroundedAction()}
	 * method of this object, and then calling the {@link GroundedAction#initParamsWithStringRep(String[])} method
	 * to set its parameters with the string representations. Consequently, the provided {@link GroundedAction}
	 * implementation must implement the {@link GroundedAction#initParamsWithStringRep(String[])} for this method to work.
	 * @param strParams the parameters of the action specified with their string representation.
	 * @return a {@link burlap.mdp.singleagent.GroundedAction} instance.
	 */
	public GroundedAction groundedAction(String...strParams){
		GroundedAction ga = this.associatedGroundedAction();
		ga.initParamsWithStringRep(strParams);
		return ga;
	}

	/**
	 * Returns all possible groundings of this action that can be applied in the provided {@link State}. To check if a grounded
	 * action is applicable in the state, the {@link #applicableInState(State, burlap.mdp.singleagent.GroundedAction)} method is checked.
	 * The default behavior of this method is to treat the parameters as possible object bindings, finding all bindings
	 * that satisfy the object class typing specified and then checking them against the {@link #applicableInState(State, burlap.mdp.singleagent.GroundedAction)}
	 * method. However, this class can also be overridden to provide custom
	 * grounding behavior or non-object based parametrization.
	 * @param s the {@link State} in which all applicable grounded actions of this {@link Action} object should be returned.
	 * @return a list of all applicable {@link GroundedAction}s of this {@link Action} object in in the given {@link State}
	 */
	public abstract List<GroundedAction> allApplicableGroundedActions(State s);
	
	
	/**
	 * Returns all {@link GroundedAction}s that are applicable in the given {@link State} for all {@link Action} objects in the provided list. This method
	 * operates by calling the {@link #allApplicableGroundedActions(State)} method on each action and adding all the results
	 * to a list that is then returned.
	 * @param actions The list of all actions for which grounded actions should be returned.
	 * @param s the state
	 * @return a {@link List} of all the {@link GroundedAction}s for all {@link Action} in the list that are applicable in the given {@link State}
	 */
	public static List<GroundedAction> getAllApplicableGroundedActionsFromActionList(List<Action> actions, State s){
		List<GroundedAction> res = new ArrayList<GroundedAction>();
		for(Action a : actions){
			res.addAll(a.allApplicableGroundedActions(s));
		}
		return res;
	}
	
	
	
	
	/**
	 * This method returns a sample from the transition dynamics from the given input state and grounded action parameters. The State
	 * object s may be directly modified in this method since the parent method ({@link #sample(State, GroundedAction)}
	 * first copies the input state to pass
	 * to this helper method. The resulting state (which may be s) should then be returned.
	 * @param s the state from which an action transition is sampled
	 * @param groundedAction the {@link burlap.mdp.singleagent.GroundedAction} specifying the parameters to use
	 * @return a sampled {@link State} from the transition dynamics
	 */
	protected abstract State sampleHelper(State s, GroundedAction groundedAction);
	
	
	
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
