package burlap.behavior.singleagent.vfa;

import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.states.State;

/**
 * An extension of {@link burlap.behavior.singleagent.vfa.ParametricScalarFunction.ParametricStateActionFunction} that
 * is differentiable.
 * Useful for state-action value function approximation (e.g., Q-value function approximation).
 * @author James MacGlashan.
 */
public interface DifferentiableStateActionValue extends ParametricScalarFunction.ParametricStateActionFunction {

	/**
	 * Returns the gradient of this function.
	 * @param s the input {@link burlap.oomdp.core.states.State}
	 * @param a the input {@link burlap.oomdp.core.AbstractGroundedAction}
	 * @return the {@link burlap.behavior.singleagent.vfa.FunctionGradient} of this function at the input
	 */
	FunctionGradient gradient(State s, AbstractGroundedAction a);

}
