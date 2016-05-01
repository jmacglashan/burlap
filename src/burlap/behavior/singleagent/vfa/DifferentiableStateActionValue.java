package burlap.behavior.singleagent.vfa;

import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;

/**
 * An extension of {@link ParametricFunction.ParametricStateActionFunction} that
 * is differentiable.
 * Useful for state-action value function approximation (e.g., Q-value function approximation).
 * @author James MacGlashan.
 */
public interface DifferentiableStateActionValue extends ParametricFunction.ParametricStateActionFunction {

	/**
	 * Returns the gradient of this function.
	 * @param s the input {@link State}
	 * @param a the input {@link AbstractGroundedAction}
	 * @return the {@link burlap.behavior.singleagent.vfa.FunctionGradient} of this function at the input
	 */
	FunctionGradient gradient(State s, AbstractGroundedAction a);

}
