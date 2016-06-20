package burlap.behavior.singleagent.learning.actorcritic;


import burlap.behavior.policy.Policy;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;


/**
 * This class provides the interface necessary for the actor portion of an Actor-Critic learning algorithm. Actors are almost entirely
 * identical to policies since they effectively specify how the agent should act; in fact, this interface extends the Policy
 * interface. However, the extra important functionality that an actor must incorporate is the ability to adjust its policy
 * in response to some critique of its behavior. In this class, this functionality should be implemented in the
 * {@link #update(EnvironmentOutcome, double)} method. The interface also provides methods for telling the actor when
 * an episode starts and ends.
 * 
 * 
 * 
 * @author James MacGlashan
 *
 */
public interface Actor extends Policy {

	/**
	 * This method is called whenever a new learning episode begins
	 * @param s the initial state of the new learning episode
	 */
	void startEpisode(State s);

	/**
	 * This method is called whenever a learning episode terminates
	 */
	void endEpisode();

	/**
	 * Causes this object to update its behavior is response to a critique of its behavior.
	 * @param eo the last transition of the environment.
	 * @param critique the critique of the agents behavior
	 */
	void update(EnvironmentOutcome eo, double critique);

	/**
	 * Used to reset any data that was created/modified during learning so that learning can be begin anew.
	 */
	void reset();

}
