package burlap.behavior.singleagent.learnfromdemo.mlirl.support;

import burlap.behavior.singleagent.vfa.FunctionGradient;
import burlap.behavior.singleagent.vfa.ParametricFunction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.GroundedAction;
import burlap.mdp.singleagent.RewardFunction;


/**
 * An interface for defining differentiable reward functions.
 * @author James MacGlashan.
 */
public interface DifferentiableRF extends RewardFunction, ParametricFunction {


	FunctionGradient gradient(State s, GroundedAction a, State sprime);


}
