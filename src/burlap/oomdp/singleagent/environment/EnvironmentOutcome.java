package burlap.oomdp.singleagent.environment;

import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;

/**
 * A class for specifying the outcome of executing an action in an {@link burlap.oomdp.singleagent.environment.Environment}.
 * The class consists of the previous state in which the action was taken; the action taken; the state to which the
 * environment transition; the reward received from the environment; and whether the new state of the environment is a
 * terminal state.
 * @author James MacGlashan.
 */
public class EnvironmentOutcome {

	/**
	 * The previous state of the environment when the action was taken.
	 */
	public State s;

	/**
	 * The action taken in the environment
	 */
	public GroundedAction a;

	/**
	 * The next state to which the environment transitioned
	 */
	public State sp;

	/**
	 * The reward received
	 */
	public double r;


	/**
	 * Whether the next state to which the environment transitioned is a terminal state (true if so, false otherwise)
	 */
	public boolean terminated;


	/**
	 * Initializes.
	 * @param s The previous state of the environment when the action was taken.
	 * @param a The action taken in the environment
	 * @param sp The next state to which the environment transitioned
	 * @param r The reward received
	 * @param terminated Whether the next state to which the environment transitioned is a terminal state (true if so, false otherwise)
	 */
	public EnvironmentOutcome(State s, GroundedAction a, State sp, double r, boolean terminated) {
		this.s = s;
		this.a = a;
		this.sp = sp;
		this.r = r;
		this.terminated = terminated;
	}
}
