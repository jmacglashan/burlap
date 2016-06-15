package burlap.behavior.singleagent.learnfromdemo.mlirl.differentiableplanners.diffvinit;

import burlap.behavior.functionapproximation.ParametricFunction;
import burlap.behavior.singleagent.learnfromdemo.mlirl.support.DifferentiableValueFunction;

/**
 * An interface for value function initialization that is differentiable with respect to some parameters. This
 * interface is useful for DifferentiableSparseSampling which may be used to learn the value of leaf nodes
 * in a finite horizon valueFunction.
 *
 * @author James MacGlashan.
 */
public interface DifferentiableVInit extends DifferentiableValueFunction, ParametricFunction {


}
