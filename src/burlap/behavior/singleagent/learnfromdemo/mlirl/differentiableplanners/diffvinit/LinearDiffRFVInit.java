package burlap.behavior.singleagent.learnfromdemo.mlirl.differentiableplanners.diffvinit;

import burlap.behavior.singleagent.learnfromdemo.mlirl.support.DifferentiableRF;
import burlap.behavior.singleagent.vfa.FunctionGradient;
import burlap.behavior.singleagent.vfa.ParametricFunction;
import burlap.behavior.singleagent.vfa.StateToFeatureVectorGenerator;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;

/**
 * A class for creating a {@link burlap.behavior.singleagent.learnfromdemo.mlirl.support.DifferentiableRF} and
 * a {@link burlap.behavior.singleagent.learnfromdemo.mlirl.differentiableplanners.diffvinit.DifferentiableVInit}
 * when the reward function and value function initialization are linear functions over some set of features.
 * The total parameter dimensionality will be the sum of the reward function feature dimension
 * and value function initialization feature dimension.
 * <p>
 * This class is useful when learning both a reward function and the shaping values at the leaf nodes of
 * a finite horizon valueFunction.
 * @author James MacGlashan.
 */
public class LinearDiffRFVInit implements DifferentiableVInit, DifferentiableRF {

	/**
	 * Whether features are based on the next state or previous state. Default is for the next state (true).
	 */
	protected boolean 								rfFeaturesAreForNextState = true;

	/**
	 * The state feature vector generator.
	 */
	protected StateToFeatureVectorGenerator rfFvGen;


	/**
	 * The state feature vector generator.
	 */
	protected StateToFeatureVectorGenerator 		vinitFvGen;


	/**
	 * The dimensionality of the reward function parameters
	 */
	protected int 									rfDim;

	/**
	 * The dimensionality of the value function initialization parameters
	 */
	protected int									vinitDim;



	protected double[]								parameters;

	protected int 									dim;


	/**
	 * Initializes a linear reward function for a given feature vector of a given dimension and linear
	 * value function initialization for a given feature vector and set of dimensions.
	 * @param rfFvGen the reward function feature vector generator
	 * @param vinitFvGen the value function initialization feature vector generator
	 * @param rfDim the reward function feature/parameter dimensionality
	 * @param vinitDim the value function initialization feature/parameter dimensionality
	 */
	public LinearDiffRFVInit(StateToFeatureVectorGenerator rfFvGen, StateToFeatureVectorGenerator vinitFvGen, int rfDim, int vinitDim) {
		this.rfFvGen = rfFvGen;
		this.vinitFvGen = vinitFvGen;
		this.rfDim = rfDim;
		this.vinitDim = vinitDim;

		this.dim = rfDim + vinitDim;
		this.parameters = new double[this.dim];

	}


	/**
	 * Initializes a linear reward function for a given feature vector of a given dimension and linear
	 * value function initialization for a given feature vector and set of dimensions.
	 * @param rfFvGen the reward function feature vector generator
	 * @param vinitFvGen the value function initialization feature vector generator
	 * @param rfDim the reward function feature/parameter dimensionality
	 * @param vinitDim the value function initialization feature/parameter dimensionality
	 * @param rfFeaturesAreForNextState if true, the the rf features are evaluated on the next state of the transition; if false then on the previous state of the transition.
	 */
	public LinearDiffRFVInit(StateToFeatureVectorGenerator rfFvGen, StateToFeatureVectorGenerator vinitFvGen, int rfDim, int vinitDim, boolean rfFeaturesAreForNextState) {
		this.rfFvGen = rfFvGen;
		this.vinitFvGen = vinitFvGen;
		this.rfDim = rfDim;
		this.vinitDim = vinitDim;
		this.rfFeaturesAreForNextState = rfFeaturesAreForNextState;

		this.dim = rfDim + vinitDim;
		this.parameters = new double[this.dim];

	}

	/**
	 * Returns whether the reward function state features are evaluated on the next state of the transition
	 * (s' of R(s,a,s')) or the previous state of the transition (s of R(s,a,s'))
	 * @return True if features are evaluated on the next state; false if they are evaluated on the previous state.
	 */
	public boolean isRfFeaturesAreForNextState() {
		return rfFeaturesAreForNextState;
	}

	public void setRfFeaturesAreForNextState(boolean rfFeaturesAreForNextState) {
		this.rfFeaturesAreForNextState = rfFeaturesAreForNextState;
	}

	public StateToFeatureVectorGenerator getRfFvGen() {
		return rfFvGen;
	}

	public void setRfFvGen(StateToFeatureVectorGenerator rfFvGen) {
		this.rfFvGen = rfFvGen;
	}

	public StateToFeatureVectorGenerator getVinitFvGen() {
		return vinitFvGen;
	}

	public void setVinitFvGen(StateToFeatureVectorGenerator vinitFvGen) {
		this.vinitFvGen = vinitFvGen;
	}

	public int getRfDim() {
		return rfDim;
	}

	public void setRfDim(int rfDim) {
		this.rfDim = rfDim;
	}

	public int getVinitDim() {
		return vinitDim;
	}

	public void setVinitDim(int vinitDim) {
		this.vinitDim = vinitDim;
	}

	public FunctionGradient gradient(State s, GroundedAction ga, State sp){

		double [] sfeatures;
		if(rfFeaturesAreForNextState){
			sfeatures = rfFvGen.generateFeatureVectorFrom(sp);
		}
		else{
			sfeatures = rfFvGen.generateFeatureVectorFrom(s);
		}

		FunctionGradient gradient = new FunctionGradient.SparseGradient(sfeatures.length);
		for(int i = 0; i < sfeatures.length; i++){
			gradient.put(i, sfeatures[i]);
		}

		return gradient;

	}





	@Override
	public double reward(State s, GroundedAction a, State sprime) {

		double [] features;
		if(this.rfFeaturesAreForNextState){
			features = this.rfFvGen.generateFeatureVectorFrom(sprime);
		}
		else{
			features = this.rfFvGen.generateFeatureVectorFrom(s);
		}
		double sum = 0.;
		for(int i = 0; i < features.length; i++){
			sum += features[i] * this.parameters[i];
		}
		return sum;

	}

	@Override
	public FunctionGradient getVGradient(State s){

		double [] vFeatures = this.vinitFvGen.generateFeatureVectorFrom(s);
		FunctionGradient gradient = new FunctionGradient.SparseGradient(vFeatures.length);

		for(int i = 0; i < vFeatures.length; i++){
			gradient.put(i+this.rfDim, vFeatures[i]);
		}
		return  gradient;

	}

	@Override
	public FunctionGradient getQGradient(State s, AbstractGroundedAction ga){
		return this.getVGradient(s);
	}



	@Override
	public double value(State s) {
		double [] features = this.vinitFvGen.generateFeatureVectorFrom(s);

		double sum = 0.;
		for(int i = 0; i < features.length; i++){
			sum += features[i] * this.parameters[i+this.rfDim];
		}
		return sum;
	}

	@Override
	public double qValue(State s, AbstractGroundedAction a) {
		return this.value(s);
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
			this.parameters[i] = 0;
		}
	}

	@Override
	public ParametricFunction copy() {
		return new LinearDiffRFVInit(this.rfFvGen, this.vinitFvGen, this.rfDim, this.vinitDim, this.rfFeaturesAreForNextState);
	}
}
