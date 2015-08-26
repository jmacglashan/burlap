package burlap.behavior.stochasticgames.agents.naiveq.history;

import burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction;

/**
 * An interface that can turn a grounded action into an integer value
 * @author James MacGlashan
 *
 */
public interface ActionIdMap {
	
	/**
	 * Returns an int value corresponding to the input action
	 * @param gsa the input action
	 * @return an int value corresponding to the input action
	 */
	public int getActionId(GroundedSGAgentAction gsa);
	
	/**
	 * Returns an int value corresponding to the input action name and parameters
	 * @param actionName the input action name
	 * @param params the input action parameters
	 * @return an int value corresponding to the input action name and parameters
	 */
	public int getActionId(String actionName, String [] params);
	
	/**
	 * The maximum number of int values for actions
	 * @return maximum number of int values for actions
	 */
	public int maxValue();
	
	/**
	 * Returns a corresponding GroundedSingleAction for a given int value
	 * @param id the int value indicating which GroundedSingleAction to return.
	 * @return a corresponding GroundedSingleAction for a given int value
	 */
	public GroundedSGAgentAction getActionForId(int id);
}
