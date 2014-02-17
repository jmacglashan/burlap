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
public abstract class Environment {

	protected State curState;
	
	
	/**
	 * Sets the current state of the environment
	 * @param s which state to set the environment to
	 */
	public void setCurStateTo(State s){
		this.curState = s;
	}
	
	
	/**
	 * Returns the current state of the environment
	 * @return the current state of the environment
	 */
	public State getCurState(){
		return curState;
	}
	
	
	/**
	 * Returns a reward function whose reward value is always whatever the last reward value of the environment is,
	 * regardless of which state, action, state parameters are passed to it.
	 * @return A reward function that returns the last reward of this environment.
	 */
	public RewardFunction getEnvironmentRewardRFWrapper(){
		return new LastRewardRF();
	}
	
	/**
	 * Returns a terminal function that returns true when the current state of the environment is terminal, regardless
	 * of the state parameter passed to the method.
	 * @return a terminal function that returns true when the current state of the environment is terminal
	 */
	public TerminalFunction getEnvironmentTerminalStateTFWrapper(){
		return new CurStateTerminalTF();
	}
	
	
	/**
	 * Tells the environment to execute the action with the given name and with the given parameters.
	 * @param aname the name of the action to execute
	 * @param params the parameters of the action
	 * @return the next state of the envionrment
	 */
	public abstract State executeAction(String aname, String [] params);
	
	/**
	 * Returns the last reward returned by the environment
	 * @return  the last reward returned by the environment
	 */
	public abstract double getLastReward();
	
	/**
	 * Returns whether the current environment state is a terminal state.
	 * @return true if the current environment state is a terminal state; false otherwise.
	 */
	public abstract boolean curStateIsTerminal();
	
	
	
	/**
	 * A reward function that returns the last reward returned by the environment, regardless
	 * of the state, action, state parameters passed to the method.
	 * @author James MacGlashan
	 *
	 */
	public class LastRewardRF implements RewardFunction{

		@Override
		public double reward(State s, GroundedAction a, State sprime) {
			return Environment.this.getLastReward();
		}
		
	}
	
	
	/**
	 * A terminal function that always returns whether the current environment state
	 * is a terminal state, regardless of the state parameter passed to the method.
	 * @author James MacGlashan
	 *
	 */
	public class CurStateTerminalTF implements TerminalFunction{

		@Override
		public boolean isTerminal(State s) {
			return Environment.this.curStateIsTerminal();
		}
		
		
	}
	
}
