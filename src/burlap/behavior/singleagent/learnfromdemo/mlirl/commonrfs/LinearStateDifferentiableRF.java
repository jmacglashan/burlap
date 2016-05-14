package burlap.behavior.singleagent.learnfromdemo.mlirl.commonrfs;

import burlap.behavior.functionapproximation.FunctionGradient;
import burlap.behavior.functionapproximation.ParametricFunction;
import burlap.behavior.functionapproximation.dense.DenseStateFeatures;
import burlap.behavior.singleagent.learnfromdemo.mlirl.support.DifferentiableRF;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;

import java.util.Arrays;

/**
 * A class for defining a linear state {@link burlap.behavior.singleagent.learnfromdemo.mlirl.support.DifferentiableRF}.
 * The features of the reward function are produced by a {@link DenseStateFeatures}.
 * By default, the reward function is defined as: R(s, a, s') = w * f(s'), where w is the weight vector (the parameters)
 * of this object, * is the dot product operator, and f(s') is the feature vector for state s'. Alternatively, the reward function
 * may be defined R(s, a, s') = w * f(s), (that is, using the feature vector for the previous state) by using the
 * {@link #LinearStateDifferentiableRF(DenseStateFeatures, int, boolean)} constructor
 * or the {@link #setFeaturesAreForNextState(boolean)}} method
 * and setting the featuresAreForNextState boolean to false.
 * @author James MacGlashan.
 */
public class LinearStateDifferentiableRF implements DifferentiableRF {

	/**
	 * Whether features are based on the next state or previous state. Default is for the next state (true).
	 */
	protected boolean 							featuresAreForNextState = true;

	/**
	 * The state feature vector generator.
	 */
	protected DenseStateFeatures fvGen;

	/**
	 * The parameters of this reward function
	 */
	protected double [] 						parameters;


	/**
	 * The dimension of this reward function
	 */
	protected int								dim;


	/**
	 * Initializes. The reward function will use the features for the next state.
	 * @param fvGen the state feature vector generator
	 * @param dim the dimensionality of the state features that will be produced
	 */
	public LinearStateDifferentiableRF(DenseStateFeatures fvGen, int dim){
		this.dim = dim;
		this.parameters = new double[dim];
		this.fvGen = fvGen;
	}

	/**
	 * Initializes.
	 * @param fvGen the state feature vector generator
	 * @param dim the dimensionality of the state features that will be produced
	 * @param featuresAreForNextState If true, then the features will be generated from the next state in the (s, a, s') transition. If false, then the previous state.
	 */
	public LinearStateDifferentiableRF(DenseStateFeatures fvGen, int dim, boolean featuresAreForNextState){
		this.featuresAreForNextState = featuresAreForNextState;
		this.dim = dim;
		this.parameters = new double[dim];
		this.fvGen = fvGen;
	}


	/**
	 * Sets whether features for the reward function are generated from the next state or previous state.
	 * @param featuresAreForNextState If true, then the features will be generated from the next state in the (s, a, s') transition. If false, then the previous state.
	 */
	public void setFeaturesAreForNextState(boolean featuresAreForNextState){
		this.featuresAreForNextState = featuresAreForNextState;
	}


	@Override
	public FunctionGradient gradient(State s, Action a, State sprime) {

		double [] features;
		if(featuresAreForNextState){
			features = fvGen.features(sprime);
		}
		else{
			features = fvGen.features(s);
		}
		FunctionGradient gradient = new FunctionGradient.SparseGradient(features.length);
		for(int i = 0; i < features.length; i++){
			gradient.put(i, features[i]);
		}

		return gradient;
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
		LinearStateDifferentiableRF rf = new LinearStateDifferentiableRF(this.fvGen, this.dim, this.featuresAreForNextState);
		rf.parameters = this.parameters.clone();
		return rf;
	}


	@Override
	public double reward(State s, Action a, State sprime){
		double [] features;
		if(this.featuresAreForNextState){
			features = fvGen.features(sprime);
		}
		else{
			features = fvGen.features(s);
		}
		double sum = 0.;
		for(int i = 0; i < features.length; i++){
			sum += features[i] * this.parameters[i];
		}
		return sum;
	}

	@Override
	public String toString() {
		return Arrays.toString(this.parameters);
	}

}
