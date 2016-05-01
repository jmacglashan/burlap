package burlap.behavior.singleagent.options.support;

import burlap.oomdp.core.state.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;

/**
 * An {@link burlap.oomdp.singleagent.environment.EnvironmentOutcome} class for reporting the effects of applying
 * an {@link burlap.behavior.singleagent.options.Option} in a given {@link burlap.oomdp.singleagent.environment.Environment}. This class extends the standard
 * {@link burlap.oomdp.singleagent.environment.EnvironmentOutcome} to include the discount to apply to the value of time steps following
 * the application of an {@link burlap.behavior.singleagent.options.Option} and the number of steps taken by the {@link burlap.behavior.singleagent.options.Option}
 * in the {@link burlap.oomdp.singleagent.environment.Environment}. The discount is therefore the gamma^t, where gamma is the
 * MDP discount factor and t is the number of time steps taken by the option. The saved reward value ({@link burlap.oomdp.singleagent.environment.EnvironmentOutcome#r})
 * for this object will also represent the cumulative discounted reward.
 * @author James MacGlashan.
 */
public class EnvironmentOptionOutcome extends EnvironmentOutcome{

	/**
	 * The discount factor to apply to the value of time steps immediately following the application of an {@link burlap.behavior.singleagent.options.Option}. Specifically,
	 * this value is gamma^t where gamma is the discount factor of the MDP and t is the number of time steps taken by the option.
	 */
	public double discount;

	/**
	 * The number of time steps for which the option was executed.
	 */
	public int numSteps;


	/**
	 * Initializes. Note that {@link #discount} of this object will be set to discountFactor^numSteps, since discountFactor is
	 * the discount factor of the MDP and {@link #discount} represents the amount values in the time step following the option
	 * application should be discounted.
	 * @param s The previous state of the environment when the action was taken.
	 * @param a The action taken in the environment
	 * @param sp The next state to which the environment transitioned
	 * @param r The reward received
	 * @param terminated Whether the next state to which the environment transitioned is a terminal state (true if so, false otherwise)
	 * @param discountFactor The discount factor of the MDP.
	 * @param numSteps The number of time steps for which the option was executed.
	 */
	public EnvironmentOptionOutcome(State s, GroundedAction a, State sp, double r, boolean terminated, double discountFactor, int numSteps) {
		super(s, a, sp, r, terminated);
		this.discount = Math.pow(discountFactor, numSteps);
		this.numSteps = numSteps;
	}
}
