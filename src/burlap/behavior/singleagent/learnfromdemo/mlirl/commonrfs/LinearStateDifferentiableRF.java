package burlap.behavior.singleagent.learnfromdemo.mlirl.commonrfs;

import burlap.behavior.singleagent.learnfromdemo.mlirl.support.DifferentiableRF;
import burlap.behavior.singleagent.vfa.StateToFeatureVectorGenerator;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;

/**
 * A class for defining a linear state {@link burlap.behavior.singleagent.learnfromdemo.mlirl.support.DifferentiableRF}.
 * The features of the reward function are produced by a {@link burlap.behavior.singleagent.vfa.StateToFeatureVectorGenerator}.
 * By default, the reward function is defined as: R(s, a, s') = w * f(s'), where w is the weight vector (the parameters)
 * of this object, * is the dot product operator, and f(s') is the feature vector for state s'. Alternatively, the reward function
 * may be defined R(s, a, s') = w * f(s), (that is, using the feature vector for the previous state) by using the
 * {@link #LinearStateDifferentiableRF(burlap.behavior.singleagent.vfa.StateToFeatureVectorGenerator, int, boolean)} constructor
 * or the {@link #setFeaturesAreForNextState(boolean)}} method
 * and setting the featuresAreForNextState boolean to false.
 * @author James MacGlashan.
 */
public class LinearStateDifferentiableRF extends DifferentiableRF {

	/**
	 * Whether features are based on the next state or previous state. Default is for the next state (true).
	 */
	protected boolean 							featuresAreForNextState = true;

	/**
	 * The state feature vector generator.
	 */
	protected StateToFeatureVectorGenerator 	fvGen;


	/**
	 * Initializes. The reward function will use the features for the next state.
	 * @param fvGen the state feature vector generator
	 * @param dim the dimensionality of the state features that will be produced
	 */
	public LinearStateDifferentiableRF(StateToFeatureVectorGenerator fvGen, int dim){
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
	public LinearStateDifferentiableRF(StateToFeatureVectorGenerator fvGen, int dim, boolean featuresAreForNextState){
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
	protected DifferentiableRF copyHelper() {
		LinearStateDifferentiableRF rf = new LinearStateDifferentiableRF(this.fvGen, this.dim, this.featuresAreForNextState);
		return rf;
	}

	public double [] getGradient(State s, GroundedAction ga, State sp){
		if(featuresAreForNextState){
			return fvGen.generateFeatureVectorFrom(sp);
		}
		else{
			return fvGen.generateFeatureVectorFrom(s);
		}
	}

	@Override
	public double reward(State s, GroundedAction a, State sprime){
		double [] features;
		if(this.featuresAreForNextState){
			features = fvGen.generateFeatureVectorFrom(sprime);
		}
		else{
			features = fvGen.generateFeatureVectorFrom(s);
		}
		double sum = 0.;
		for(int i = 0; i < features.length; i++){
			sum += features[i] * this.parameters[i];
		}
		return sum;
	}

}
