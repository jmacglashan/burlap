package burlap.behavior.singleagent.learnfromdemo.mlirl.support;

import burlap.behavior.functionapproximation.FunctionGradient;
import burlap.behavior.functionapproximation.ParametricFunction;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.RewardFunction;


/**
 * An interface for defining differentiable reward functions.
 * @author James MacGlashan.
 */
public interface DifferentiableRF extends RewardFunction, ParametricFunction {


	FunctionGradient gradient(State s, Action a, State sprime);


}
