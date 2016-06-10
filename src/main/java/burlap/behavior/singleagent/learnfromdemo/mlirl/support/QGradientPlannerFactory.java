package burlap.behavior.singleagent.learnfromdemo.mlirl.support;

import burlap.behavior.singleagent.learnfromdemo.mlirl.MLIRLRequest;
import burlap.behavior.singleagent.learnfromdemo.mlirl.differentiableplanners.DifferentiableVI;
import burlap.statehashing.HashableStateFactory;
import burlap.mdp.auxiliary.common.NullTermination;
import burlap.mdp.core.TerminalFunction;

/**
 * A factory for generating {@link burlap.behavior.singleagent.learnfromdemo.mlirl.support.QGradientPlanner} objects.
 * This class is use for {@link burlap.behavior.singleagent.learnfromdemo.mlirl.MultipleIntentionsMLIRL}, so that it
 * can generate a different differentiable valueFunction for each cluster; that way, after a maximization step,
 * it can query the policy for each cluster in any state without replanning,rather than using a single valueFunction
 * instance that would require replanning for each cluster (since it would have to switch the reward function).
 * @author James MacGlashan.
 */
public interface QGradientPlannerFactory {

	/**
	 * Returns a {@link burlap.behavior.singleagent.learnfromdemo.mlirl.support.QGradientPlanner} for an
	 * {@link burlap.behavior.singleagent.learnfromdemo.mlirl.MLIRLRequest} object's domain,
	 * reward function, discount factor, and Boltzmann beta parameter.
	 * @param request the request defining the problem the valueFunction should solve.
	 * @return a {@link burlap.behavior.singleagent.learnfromdemo.mlirl.support.QGradientPlanner} instance.
	 */
	public QGradientPlanner generateDifferentiablePlannerForRequest(MLIRLRequest request);


	/**
	 * A {@link burlap.behavior.singleagent.learnfromdemo.mlirl.differentiableplanners.DifferentiableVI} factory.
	 */
	public static class DifferentiableVIFactory implements QGradientPlannerFactory{

		/**
		 * The {@link burlap.statehashing.HashableStateFactory} used by the valueFunction.
		 */
		protected HashableStateFactory hashingFactory;

		/**
		 * The value function change threshold to stop VI. Default is 0.01.
		 */
		protected double maxDelta = 0.01;

		/**
		 * The maximum allowed number of VI iterations. Default is 500.
		 */
		protected int maxIterations = 500;


		/**
		 * The terminal function that the valueFunction uses. Default is a a {@link burlap.mdp.auxiliary.common.NullTermination}.
		 */
		protected TerminalFunction tf = new NullTermination();


		/**
		 * Initializes the factory with the given {@link burlap.statehashing.HashableStateFactory}.
		 * The terminal function will be defaulted to a {@link burlap.mdp.auxiliary.common.NullTermination};
		 * value function change threshold to 0.01; and the max VI iterations to 500.
		 * @param hashingFactory the {@link burlap.statehashing.HashableStateFactory} to use for planning.
		 */
		public DifferentiableVIFactory(HashableStateFactory hashingFactory){
			this.hashingFactory = hashingFactory;
		}


		/**
		 * Initializes.
		 * @param hashingFactory the {@link burlap.statehashing.HashableStateFactory} to use for planning.
		 * @param tf The terminal function that the generated planners use.
		 * @param maxDelta The value function change threshold to stop VI.
		 * @param maxIterations The maximum allowed number of VI iterations
		 */
		public DifferentiableVIFactory(HashableStateFactory hashingFactory, TerminalFunction tf, double maxDelta, int maxIterations){
			this.hashingFactory = hashingFactory;
			this.maxDelta = maxDelta;
			this.maxIterations = maxIterations;
			this.tf = tf;
		}

		@Override
		public QGradientPlanner generateDifferentiablePlannerForRequest(MLIRLRequest request) {
			return new DifferentiableVI(request.getDomain(), request.getRf(), request.getGamma(),
					request.getBoltzmannBeta(), this.hashingFactory, this.maxDelta, this.maxIterations);
		}
	}

}
