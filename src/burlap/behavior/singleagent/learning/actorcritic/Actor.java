package burlap.behavior.singleagent.learning.actorcritic;


import burlap.behavior.singleagent.Policy;
import burlap.oomdp.singleagent.Action;


/**
 * This class provides interface necessary for the actor portion of an Actor-Critic learning algorithm. Actors are almost entirely
 * identical to policies since they effectively specify how the agent should act; in fact, this abstract class extends the Policy
 * class. However, the extra important functionality that an actor must incorporate is the ability to adjust its policy
 * in response to some critique of its behavior. In this class, this functionality should be implemented in the
 * {@link #updateFromCritqique(CritiqueResult)} method.
 * 
 * 
 * 
 * @author James MacGlashan
 *
 */
public abstract class Actor extends Policy {

	/**
	 * Causes this object to update its behavior is response to a critique of its behavior.
	 * @param critqiue the critique of the agents behavior represented by a {@link CritiqueResult} object
	 */
	public abstract void updateFromCritqique(CritiqueResult critqiue);
	
	/**
	 * This method allows the actor to utilize actions that are not apart of the domain definition.
	 * @param a an action not apart of the of the domain definition that this actor should be able to use.
	 */
	public abstract void addNonDomainReferencedAction(Action a);
	
	
	/**
	 * Used to reset any data that was created/modified during learning so that learning can be begin anew.
	 */
	public abstract void resetData();

}
