package burlap.mdp.singleagent.environment;

import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.GroundedAction;

/**
 * A class for specifying the outcome of executing an action in an {@link burlap.mdp.singleagent.environment.Environment}.
 * The class consists of the previous environment observation (as a {@link State}) in which the action was taken;
 * the action taken (as a {@link burlap.mdp.singleagent.GroundedAction}); the next environment observation (also a {@link State}
 * following the action; the reward received from the environment; and whether the new state of the environment is a
 * terminal state.
 * @author James MacGlashan.
 */
public class EnvironmentOutcome {

	/**
	 * The previous environment observation (as a {@link State} when the action was taken.
	 */
	public State o;

	/**
	 * The action taken in the environment
	 */
	public GroundedAction a;

	/**
	 * The next environment observation (as a {@link State}) following the action's execution.
	 */
	public State op;

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
	 * @param o The previous state of the environment when the action was taken.
	 * @param a The action taken in the environment
	 * @param op The next state to which the environment transitioned
	 * @param r The reward received
	 * @param terminated Whether the next state to which the environment transitioned is a terminal state (true if so, false otherwise)
	 */
	public EnvironmentOutcome(State o, GroundedAction a, State op, double r, boolean terminated) {
		this.o = o;
		this.a = a;
		this.op = op;
		this.r = r;
		this.terminated = terminated;
	}
}
