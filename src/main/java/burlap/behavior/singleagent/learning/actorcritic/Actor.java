package burlap.behavior.singleagent.learning.actorcritic;


import burlap.behavior.policy.Policy;


/**
 * This class provides interface necessary for the actor portion of an Actor-Critic learning algorithm. Actors are almost entirely
 * identical to policies since they effectively specify how the agent should act; in fact, this abstract class extends the Policy
 * class. However, the extra important functionality that an actor must incorporate is the ability to adjust its policy
 * in response to some critique of its behavior. In this class, this functionality should be implemented in the
 * {@link #update(CritiqueResult)} method.
 * 
 * 
 * 
 * @author James MacGlashan
 *
 */
public interface Actor extends Policy {

	/**
	 * Causes this object to update its behavior is response to a critique of its behavior.
	 * @param critique the critique of the agents behavior represented by a {@link CritiqueResult} object
	 */
	void update(CritiqueResult critique);

	/**
	 * Used to reset any data that was created/modified during learning so that learning can be begin anew.
	 */
	void reset();

}
