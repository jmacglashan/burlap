package burlap.behavior.singleagent.vfa.fourier;

import burlap.behavior.learningrate.LearningRate;
import burlap.behavior.singleagent.learning.tdmethods.vfa.GradientDescentSarsaLam;
import burlap.behavior.singleagent.vfa.FeatureDatabase;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.state.State;


/**
 * This {@link LearningRate} implementation provides a wrapper around a source {@link LearningRate} that should be used whenever using {@link FourierBasis} features
 * with an algorithm like {@link GradientDescentSarsaLam}. This implementation will query the source {@link LearningRate} implementation for its vfa feature-wise
 * learning rate value and then scale it by the inverse of the L2 norm of the coefficient vector that is associated with the Fourier basis function for that feature id.
 * That is, if alpha(j) is the learning rate returned by the source {@link LearningRate} implementation for basis function (feature id) j, then
 * this implementation will return alpha(j) / ||c_j||, where c_j is the coefficient vector associated with Fourier baiss function j.
 * <p>
 * Since this wrapper operates on state-action features, it will throw a runtime exception if it is queried for OO-MDP {@link State}-wise learning rate peek and poll
 * methods ({@link #peekAtLearningRate(State, AbstractGroundedAction)} and {@link #pollLearningRate(int, State, AbstractGroundedAction)}, repsectively). Instead, clients
 * should only call the {@link #peekAtLearningRate(int)} and {@link #pollLearningRate(int, int)} methods.
 * 
 * 
 * @author James MacGlashan
 *
 */
public class FourierBasisLearningRateWrapper implements LearningRate {

	
	/**
	 * The source {@link LearningRate} function that is queried.
	 */
	protected LearningRate			sourceLearningRateFunction;
	
	/**
	 * The Fourier basis functions that are used.
	 */
	protected FourierBasis			fouierBasisFunctions;
	
	
	/**
	 * Initializes.
	 * @param sourceLearningRateFunction the source {@link LearningRate} function that will be scaled.
	 * @param fouierBasisFunctions the {@link FourierBasis} {@link FeatureDatabase} that defines the Fourier basis functions and their coefficient vectors.
	 */
	public FourierBasisLearningRateWrapper(LearningRate sourceLearningRateFunction, FourierBasis fouierBasisFunctions){
		this.sourceLearningRateFunction = sourceLearningRateFunction;
		this.fouierBasisFunctions = fouierBasisFunctions;
	}
	
	@Override
	public double peekAtLearningRate(State s, AbstractGroundedAction ga) {
		throw new UnsupportedOperationException("FourierBasisLearningRateWrapper is not defined for returning learning rates on whole OO-MDP state objects. Client code should use the feature-wise peek method instead");
	}

	@Override
	public double pollLearningRate(int agentTime, State s, AbstractGroundedAction ga) {
		throw new UnsupportedOperationException("FourierBasisLearningRateWrapper is not defined for returning learning rates on whole OO-MDP state objects. Client code should use the feature-wise poll method instead");
	}

	@Override
	public double peekAtLearningRate(int featureId) {
		double l = this.sourceLearningRateFunction.peekAtLearningRate(featureId);
		double norm = this.fouierBasisFunctions.coefficientNorm(featureId);
		if(norm == 0){
			return l;
		}
		double nl = l / norm;
		return nl;
	}

	@Override
	public double pollLearningRate(int agentTime, int featureId) {
		double l = this.sourceLearningRateFunction.pollLearningRate(agentTime, featureId);
		double norm = this.fouierBasisFunctions.coefficientNorm(featureId);
		if(norm == 0){
			return l;
		}
		double nl = l / norm;
		return nl;
	}

	@Override
	public void resetDecay() {
		this.sourceLearningRateFunction.resetDecay();
	}

}
