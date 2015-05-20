package burlap.behavior.singleagent.options;

import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.RewardFunction;


/**
 * If an option deterministically terminates with a fixed number of steps, then it may be useful
 * for an option to immediately transition from the state in which the option was initiated to the
 * end terminal state, rather than having to simulate each step of execution. This interface provides a means
 * for defining the transition dynamics, the number of steps and cumulative reward, so that
 * an option can immediately take the agent to the next state without having to perform simulation.
 * After implementing this interface, it can then be provided to any {@link Option} object
 * to prevent the option from performing the simulation.
 * If the options path and number of steps cannot be guaranteed in advance, then this interface
 * should not be implemented.
 *
 * @author James MacGlashan
 *
 */
public interface DirectOptionTerminateMapper {

	/**
	 * Returns the termination state that will result from applying the option associated with this object
	 * in the given initiation state.
	 * @param s the initiation state in which the option associated with this object will be applied.
	 * @return the resulting terminal state of the option.
	 */
	public State generateOptionTerminalState(State s);
	
	/**
	 * Returns the number of steps that would have occurred for taking the option associated with this object
	 * from state <code>s</code> and terminating in state <code>sp</code>.
	 * @param s the initiation state in which the option associated with this object will be applied.
	 * @param sp the terminal state the option will reach
	 * @return the number of steps that would have occurred
	 */
	public int getNumSteps(State s, State sp);
	
	/**
	 * Returns the cumulative discounted reward that would be received from applying the option associated with this object
	 * in the given initiation state.
	 * @param s the initiation state in which the option associated with this object will be applied.
	 * @param sp the terminal state the option will reach
	 * @param rf the reward function being used
	 * @param discount the discount factor
	 * @return the cumulative discounted reward
	 */
	public double getCumulativeReward(State s, State sp, RewardFunction rf, double discount);
	
}
