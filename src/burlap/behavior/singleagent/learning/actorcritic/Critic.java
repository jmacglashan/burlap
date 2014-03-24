package burlap.behavior.singleagent.learning.actorcritic;

import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;


/**
 * This interface provides the methods necessary for implementing the critic part of an actor-critic learning algorithm. The critic
 * is responsible for observing behavior (state, action, state tuples) and returning a critique of that behavior. Typically,
 * Critic objects will need to take as input a reward function to judge this behavior.
 * 
 * 
 * @author James MacGlashan
 *
 */
public interface Critic {
	
	/**
	 * This method allows the critic to critique actions that are not apart of the domain definition.
	 * @param a a an action not apart of the of the domain definition that this critic should be able to crique.
	 */
	public void addNonDomainReferencedAction(Action a);
	
	
	/**
	 * This method is called whenever a new learning episode begins
	 * @param s the initial state of the new learning episode
	 */
	public void initializeEpisode(State s);
	
	/**
	 * This method is called whenever a learning episode terminates
	 */
	public void endEpisode();

	
	/**
	 * This method's implementation provides the critique for some specific instance of the behavior.
	 * @param s an input state
	 * @param ga an action taken in s
	 * @param sprime the state the agent transitioned to for taking action ga in state s
	 * @return the critique of this behavior.
	 */
	public CritiqueResult critiqueAndUpdate(State s, GroundedAction ga, State sprime);
	
	/**
	 * Used to reset any data that was created/modified during learning so that learning can be begin anew.
	 */
	public abstract void resetData();
	
}
