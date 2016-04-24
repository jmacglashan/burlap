package burlap.oomdp.singleagent;

import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.AbstractObjectParameterizedGroundedAction;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;

import java.util.List;

/**
 * A {@link burlap.oomdp.singleagent.GroundedAction} is a high-level abstract class implementation of a {@link burlap.oomdp.core.AbstractGroundedAction}
 * that is closely associated with single-agent {@link burlap.oomdp.singleagent.Action} definitions. The role of
 * a {@link burlap.oomdp.singleagent.GroundedAction} is to provide a reference to its corresponding {@link burlap.oomdp.singleagent.Action}
 * definition and also provide parameter assignments with which its {@link burlap.oomdp.singleagent.Action} should be applied.
 * The set of possible {@link burlap.oomdp.singleagent.GroundedAction} instances specifying the different possible parameter assignments
 * will be generated by the associated
 * {@link burlap.oomdp.singleagent.Action#getAllApplicableGroundedActions(burlap.oomdp.core.states.State)} method. See
 * the {@link burlap.oomdp.singleagent.Action} class documentation for more information on implementing parameterized
 * {@link burlap.oomdp.singleagent.Action} definitions.
 * <p>
 * Implementing this class requires implementing three methods from {@link burlap.oomdp.core.AbstractGroundedAction}
 * that are not implemented by {@link burlap.oomdp.singleagent.GroundedAction}:<p>
 * {@link #copy()}<p>
 * {@link #initParamsWithStringRep(String[])} and<p>
 * {@link #getParametersAsString()}.<p>
 * If the {@link burlap.oomdp.singleagent.Action} with which the {@link burlap.oomdp.singleagent.GroundedAction} is associated
 * is not parameterized, then you can return the concrete class {@link burlap.oomdp.singleagent.common.SimpleGroundedAction}.
 * Otherwise, you will need to make your own subclass of {@link burlap.oomdp.singleagent.GroundedAction} and implement those methods.
 * Note that you can have your implementation store the parameter information anyway you like as long the {@link #copy()} method
 * creates a version instance of your {@link burlap.oomdp.singleagent.GroundedAction} implementation that copies over
 * the parameter information. Additionally, for full support with all BURLAP tools, it should be possible to initialize
 * the parameters with a String array using the {@link #initParamsWithStringRep(String[])} and get a String array representation
 * of them with the {@link #getParametersAsString()}.
 * <p>
 * In addition to specifying parameter assignment information for a {@link burlap.oomdp.singleagent.Action} definition,
 * this class also provides a number of useful shortcut methods for evaluating the {@link burlap.oomdp.singleagent.GroundedAction}.
 * Specifically, see the<p>
 * {@link #executeIn(burlap.oomdp.core.states.State)}<p>
 * {@link #executeIn(burlap.oomdp.singleagent.environment.Environment)}<p>
 * {@link #getTransitions(burlap.oomdp.core.states.State)} and<p>
 * {@link #applicableInState(burlap.oomdp.core.states.State)} <p>
 * methods, which call the associated {@link burlap.oomdp.singleagent.Action} methods providing this instance
 * as the set of parameters to use. Note that the {@link #getTransitions(burlap.oomdp.core.states.State)} method
 * will throw a runtime exception if the associated {@link burlap.oomdp.singleagent.Action} definition does
 * not implement the {@link burlap.oomdp.singleagent.FullActionModel} interface.
 *

 * @author James MacGlashan
 *
 */
public abstract class GroundedAction implements AbstractGroundedAction{

	/**
	 * The action object for this grounded action
	 */
	public Action action;


	/**
	 * Initializes with the {@link burlap.oomdp.singleagent.Action} definition with which this {@link burlap.oomdp.singleagent.GroundedAction}
	 * is associated.
	 * @param action the associated {@link burlap.oomdp.singleagent.Action} definition.
	 */
	public GroundedAction(Action action){
		this.action = action;
	}

	
	/**
	 * Returns the action name for this grounded action.
	 * @return the action name for this grounded action.
	 */
	public String actionName(){
		return this.action.getName();
	}

	@Override
	public boolean isParameterized() {
		return this.action.isParameterized();
	}

	@Override
	public String toString(){
		String [] strParams = this.getParametersAsString();
		if(strParams == null || strParams.length == 0) {
			return this.actionName();
		}
		else{
			StringBuilder builder = new StringBuilder();
			builder.append(this.actionName());
			for(String param : strParams){
				builder.append(" ").append(param);
			}
			return builder.toString();
		}
	}

	public boolean applicableInState(State s){
		return this.action.applicableInState(s, this);
	}

	@Override
	public abstract GroundedAction copy();


	/**
	 * Executes this grounded action in the specified {@link burlap.oomdp.singleagent.environment.Environment}.
	 * @param env the {@link burlap.oomdp.singleagent.environment.Environment} in which the action is to be executed.
	 * @return an {@link burlap.oomdp.singleagent.environment.EnvironmentOutcome} specifying the outcome of this action.
	 */
	public EnvironmentOutcome executeIn(Environment env){
		return this.action.performInEnvironment(env, this);
	}

	/**
	 * Executes the grounded action on a given state
	 * @param s the state on which to execute the action
	 * @return The state after the action has been executed
	 */
	public State executeIn(State s){
		return action.performAction(s, this);
	}


	/**
	 * Returns the full set of possible state transitions when this {@link burlap.oomdp.singleagent.GroundedAction} is applied in
	 * the given state. If the underlying {@link burlap.oomdp.singleagent.Action} definition does not implement
	 * {@link burlap.oomdp.singleagent.FullActionModel}, then a runtime exception will be thrown instead.
	 * @param s the source state from which the transitions should be computed.
	 * @return a {@link java.util.List} of {@link burlap.oomdp.core.TransitionProbability} objects specifying all state transitions from the input state that have non-zero probability.
	 */
	public List<TransitionProbability> getTransitions(State s){
		if(!(this.action instanceof FullActionModel)){
			throw new RuntimeException("GroundedAction cannot return the full state transitions because action " + this.actionName() + " does " +
					"not implement the FullActionModel interface.");
		}
		return ((FullActionModel)this.action).getTransitions(s, this);
	}


	/**
	 * A helper method that handles action translate in case this {@link burlap.oomdp.singleagent.GroundedAction} implements
	 * {@link burlap.oomdp.core.AbstractObjectParameterizedGroundedAction}. Works by calling the
	 * {@link burlap.oomdp.core.AbstractObjectParameterizedGroundedAction.Helper#translateParameters(burlap.oomdp.core.AbstractGroundedAction, burlap.oomdp.core.states.State, burlap.oomdp.core.states.State)}
	 * method with this object as the action to translate.
	 * If this is a {@link burlap.oomdp.core.AbstractObjectParameterizedGroundedAction}, then a new {@link burlap.oomdp.singleagent.GroundedAction} with its object parameters mapped to the object names sin the target state
	 * is returned.
	 * @param source the source state in which this {@link burlap.oomdp.singleagent.GroundedAction}'s parameters were bound.
	 * @param target the target state in which the returned {@link burlap.oomdp.singleagent.GroundedAction} will have its parameters bound.
	 * @return If this is a {@link burlap.oomdp.core.AbstractObjectParameterizedGroundedAction}, then a new {@link burlap.oomdp.singleagent.GroundedAction} with its object parameters mapped to the object names sin the target state
	 */
	public GroundedAction translateParameters(State source, State target){
		return (GroundedAction)AbstractObjectParameterizedGroundedAction.Helper.translateParameters(this, source, target);
	}
	
	@Override
	public int hashCode(){
		return this.action.getName().hashCode();
	}
	
	
	@Override
	public boolean equals(Object other){

		if(this == other){
			return true;
		}

		if(!(other instanceof GroundedAction)){
			return false;
		}

		GroundedAction go = (GroundedAction)other;

		return this.actionName().equals(((GroundedAction) other).actionName());

	}






	
}
