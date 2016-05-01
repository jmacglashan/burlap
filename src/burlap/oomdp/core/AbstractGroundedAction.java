package burlap.oomdp.core;

import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.stochasticgames.JointAction;

/**
 * This is an interface for grounded actions. A grounded action is a reference to an action definition along with the specific parameters with which the action
 * is to be applied. Subclasses for this class include the single-agent action grounding ({@link GroundedAction}), an action grounding for a specific agent
 * in a stochastic game {@link burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction}, and a joint action in a stochastic game ({@link JointAction}).
 * @author James MacGlashan
 *
 */
public interface AbstractGroundedAction {

	
	/**
	 * Returns the action name for this grounded action.
	 * @return the action name for this grounded action.
	 */
	String actionName();

	
	/**
	 * Returns a copy of this grounded action.
	 * @return a copy of this grounded action.
	 */
	AbstractGroundedAction copy();
	
	
	/**
	 * Returns true if this action uses parameters
	 * @return true if this action uses parameters; false otherwise
	 */
	boolean isParameterized();


	/**
	 * Initializes the parameter values of this {@link AbstractGroundedAction} according
	 * to the provided string representation of their values.
	 * @param params an array in which each element is the string representation of one of this {@link AbstractGroundedAction}'s values
	 */
	void initParamsWithStringRep(String [] params);


	/**
	 * Returns an array of string representations of this {@link AbstractGroundedAction}'s parameters
	 * @return an array of string representations of this {@link AbstractGroundedAction}'s parameters
	 */
	String [] getParametersAsString();


	
}
