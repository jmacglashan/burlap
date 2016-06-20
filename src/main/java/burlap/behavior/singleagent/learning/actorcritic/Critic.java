package burlap.behavior.singleagent.learning.actorcritic;

import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;


/**
 * This interface provides the methods necessary for implementing the critic part of an actor-critic learning algorithm. The critic
 * is responsible for observing behavior and returning a critique of that behavior. The interface also provides methods for telling the critic when
 * an episode starts and ends.
 * 
 * 
 * @author James MacGlashan
 *
 */
public interface Critic {

	
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
	 * This method's implementation provides the critique for some specific instance of the behavior. This method
	 * may modify this critic, so that subsequent calls with the same {@link EnvironmentOutcome} can produce
	 * different critiques.
	 * @param eo the {@link EnvironmentOutcome} specifying the event
	 * @return the critique of this behavior.
	 */
	double critique(EnvironmentOutcome eo);
	
	/**
	 * Used to reset any data that was created/modified during learning so that learning can be begin anew.
	 */
	void reset();
	
}
