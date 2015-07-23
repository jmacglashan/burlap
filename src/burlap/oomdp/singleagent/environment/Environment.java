package burlap.oomdp.singleagent.environment;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


/**
 * Environments define a current state and manage state and reward transitions when an action is executed in the environment through
 * the {@link #executeAction(burlap.oomdp.singleagent.GroundedAction)} method. {@link burlap.oomdp.singleagent.environment.Environment}
 * instances are what learning algorithms implementing {@link burlap.behavior.singleagent.learning.LearningAgent} interact with
 * the environment around them. Maintaining an Environment ensures that transitions are protected from an agent manipulating the state
 * and are also useful when a BURLAP agent is interacting with external or real time systems such as robotics. Environments
 * also make it easy to use a planning algorithm to compute a {@link burlap.behavior.singleagent.Policy} using some model of the world
 * and then have that policy executed in an {@link burlap.oomdp.singleagent.environment.Environment} that may behave differently
 * than the model (e.g., robotics operating in the real world). {@link burlap.oomdp.singleagent.environment.Environment} implementations
 * also make it easy to train a {@link burlap.behavior.singleagent.learning.LearningAgent} in one {@link burlap.oomdp.singleagent.environment.Environment}
 * and then use them in a new {@link burlap.oomdp.singleagent.environment.Environment} after learning.
 * <br/><br/>
 * If you wish to use a simulated BURLAP {@link burlap.oomdp.core.Domain} to manage the transitions and reward function, you should
 * consider using the {@link burlap.oomdp.singleagent.environment.SimulatedEnvironment} implementation.
 * 
 * @author James MacGlashan
 *
 */
public interface Environment {

	
	/**
	 * Returns the current state of the environment
	 * @return the current state of the environment
	 */
	State getCurState();

	
	
	/**
	 * Executes the specified action in this environment
	 * @param ga the GroundedAction that is to be performed in this environment.
	 * @return the resulting state from applying the given GroundedAction in this environment.
	 */
	EnvironmentOutcome executeAction(GroundedAction ga);
	

	
	/**
	 * Returns the last reward returned by the environment
	 * @return  the last reward returned by the environment
	 */
	double getLastReward();
	
	/**
	 * Returns whether the current environment state is a terminal state.
	 * @return true if the current environment state is a terminal state; false otherwise.
	 */
	boolean curStateIsTerminal();


	/**
	 * Resets this environment to some initial state, if the functionality exists.
	 */
	void resetEnvironment();
	
}
