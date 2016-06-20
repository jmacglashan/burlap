package burlap.behavior.singleagent.learnfromdemo.mlirl.differentiableplanners.diffvinit;

import burlap.behavior.functionapproximation.FunctionGradient;
import burlap.behavior.functionapproximation.ParametricFunction;
import burlap.behavior.functionapproximation.dense.DenseStateFeatures;
import burlap.mdp.core.state.State;

/**
 * A class for defining a (differentiable) linear function over state features for value function initialization. This class is useful
 * for learning the value function initialization for leaf nodes of a finite horizon valueFunction with {@link burlap.behavior.singleagent.learnfromdemo.mlirl.MLIRL}.
 * @author James MacGlashan.
 */
public class LinearStateDiffVF implements DifferentiableVInit {


	/**
	 * The state feature vector generator over which the linear function operates
	 */
	protected DenseStateFeatures fvgen;

	protected int dim;
	protected double [] parameters;


	/**
	 * Initializes with the state feature vector generator over which the linear function is defined and the dimensionality of it.
	 * @param fvgen the state feature vector generator over which the linear function is defined.
	 * @param dim the dimensionality of the feature vector/parameters
	 */
	public LinearStateDiffVF(DenseStateFeatures fvgen, int dim){
		this.dim = dim;
		this.parameters = new double[dim];
		this.fvgen = fvgen;
	}


	@Override
	public int numParameters() {
		return this.dim;
	}

	@Override
	public double getParameter(int i) {
		return this.parameters[i];
	}

	@Override
	public void setParameter(int i, double p) {
		this.parameters[i] = p;
	}

	@Override
	public void resetParameters() {
		for(int i = 0; i < this.parameters.length; i++){
			this.parameters[i] = 0.;
		}
	}

	@Override
	public ParametricFunction copy() {
		return null;
	}

	@Override
	public FunctionGradient valueGradient(State s) {
		double [] fvec = this.fvgen.features(s);
		FunctionGradient gradient = new FunctionGradient.SparseGradient();
		for(int i = 0; i < fvec.length; i++){
			gradient.put(i, fvec[i]);
		}
		return gradient;
	}


	@Override
	public double value(State s) {

		double [] features = this.fvgen.features(s);

		double sum = 0.;
		for(int i = 0; i < features.length; i++){
			sum += features[i] * this.parameters[i];
		}
		return sum;
	}


}
