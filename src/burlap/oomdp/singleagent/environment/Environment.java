package burlap.oomdp.singleagent.environment;

import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


/**
 * In some cases it may be useful to have agents interact with an external environment that handles the current state, execution of actions, and rewards
 * and maintains other important external information, rather than use the standard Domain, Action, RewardFunction TerminalFunction paradigm of BURLAP.
 * In particular, Environments may be useful in real time systems in which actions take some length of time to occur (such as physical robots), or in
 * which human interaction plays a critical role in affecting state, rewards, or termination events. This Environment abstract class provides an interface
 * to manage these kinds of scenarios.
 * 
 * @author James MacGlashan
 *
 */
public interface Environment {


	
	/**
	 * Returns the current state of the environment
	 * @return the current state of the environment
	 */
	public State getCurState();

	
	
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


	
	/**
	 * A reward function that returns the last reward returned by the environment, regardless
	 * of the state, action, state parameters passed to the method.
	 * @author James MacGlashan
	 *
	 */
	public static class LastRewardRF implements RewardFunction{

		protected Environment env;

		public LastRewardRF(Environment env){
			this.env = env;
		}


		@Override
		public double reward(State s, GroundedAction a, State sprime) {
			return this.env.getLastReward();
		}
		
	}
	
	
	/**
	 * A terminal function that always returns whether the current environment state
	 * is a terminal state, regardless of the state parameter passed to the method.
	 * @author James MacGlashan
	 *
	 */
	public static class CurStateTerminalTF implements TerminalFunction{

		protected Environment env;

		public CurStateTerminalTF(Environment env){
			this.env = env;
		}

		@Override
		public boolean isTerminal(State s) {
			return this.env.curStateIsTerminal();
		}
		
		
	}
	
}
