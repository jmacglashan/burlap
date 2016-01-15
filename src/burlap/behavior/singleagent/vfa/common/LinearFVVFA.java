package burlap.behavior.singleagent.vfa.common;

import burlap.behavior.singleagent.vfa.DifferentiableStateActionValue;
import burlap.behavior.singleagent.vfa.DifferentiableStateValue;
import burlap.behavior.singleagent.vfa.FunctionGradient;
import burlap.behavior.singleagent.vfa.StateToFeatureVectorGenerator;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;

import java.util.HashMap;
import java.util.Map;

/**
 * This class can be used to perform linear value function approximation, either for a states or state-actions (Q-values).
 * It takes as input a {@link burlap.behavior.singleagent.vfa.StateToFeatureVectorGenerator} which defines
 * the state features on which linear function approximation is performed. In the case of Q-value
 * function approximation, the state features are replicated for each action with all other action's associated
 * state features set to zero, thereby allowing for unique predictions for each action.
 * <br/><br/>
 * This class can be used for either state-value functions or state-action-value functions, but only one of them.
 * Which one is used is determined implicitly by whether the first function input is set with the
 * {@link #functionInput(burlap.oomdp.core.states.State)} method or the {@link #functionInput(burlap.oomdp.core.states.State, burlap.oomdp.core.AbstractGroundedAction)}
 * method.
 * @author James MacGlashan.
 */
public class LinearFVVFA implements DifferentiableStateValue, DifferentiableStateActionValue{


	/**
	 * The state feature vector generator used for linear value function approximation.
	 */
	protected StateToFeatureVectorGenerator				fvGen;

	/**
	 * A feature index offset for each action when using Q-value function approximation.
	 */
	protected Map<AbstractGroundedAction, Integer> 		actionOffset = new HashMap<AbstractGroundedAction, Integer>();

	/**
	 * The function weights when performing state value function approximation.
	 */
	protected double[]									stateWeights;


	/**
	 * The function weights when performing Q-value function approximation.
	 */
	protected double[]									stateActionWeights;

	/**
	 * A default weight value for the functions weights.
	 */
	protected double									defaultWeight = 0.0;



	protected double[]								currentStateFeatures;
	protected int									currentActionOffset = -1;
	protected double								currentValue;
	protected FunctionGradient						currentGradient = null;


	@Override
	public double functionInput(State s, AbstractGroundedAction a) {
		this.currentStateFeatures = this.fvGen.generateFeatureVectorFrom(s);
		this.currentActionOffset = this.getActionOffset(a);
		int indOff = this.currentActionOffset*this.currentStateFeatures.length;
		double val = 0;
		for(int i = 0; i < this.currentStateFeatures.length; i++){
			val += this.currentStateFeatures[i] * this.stateActionWeights[i+indOff];
		}
		this.currentValue = val;
		this.currentGradient = null;
		return this.currentValue;
	}

	@Override
	public double functionInput(State s) {
		this.currentStateFeatures = this.fvGen.generateFeatureVectorFrom(s);
		this.currentActionOffset = 0;
		if(this.stateWeights == null){
			this.stateWeights = new double[this.currentStateFeatures.length];
			for(int i = 0; i < this.stateWeights.length; i++){
				this.stateWeights[i] = this.defaultWeight;
			}
		}
		double val = 0;
		for(int i = 0; i < this.currentStateFeatures.length; i++){
			val += this.currentStateFeatures[i] * this.stateWeights[i];
		}
		this.currentValue = val;
		this.currentGradient = null;
		return this.currentValue;
	}

	@Override
	public FunctionGradient computeGradient() {

		if(currentGradient != null){
			return this.currentGradient;
		}

		FunctionGradient gradient = new FunctionGradient(this.currentStateFeatures.length);
		int sIndOffset = this.currentActionOffset*this.currentStateFeatures.length;
		for(int i = 0; i < this.currentStateFeatures.length; i++){
			gradient.put(i+sIndOffset, this.currentStateFeatures[i]);
		}

		this.currentGradient = gradient;

		return gradient;
	}

	@Override
	public double functionValue() {
		if(this.currentStateFeatures == null){
			throw new RuntimeException("Cannot return function value because no input has been specified.");
		}
		return this.currentValue;
	}

	@Override
	public int numParameters() {
		if(this.stateWeights != null){
			return this.stateWeights.length;
		}
		else if(this.stateActionWeights != null){
			return this.stateActionWeights.length;
		}
		return 0;
	}

	@Override
	public double getParameter(int i) {
		if(this.stateWeights != null){
			if(i < this.stateWeights.length){
				return this.stateWeights[i];
			}
		}
		else if(this.stateActionWeights != null){
			if(i < this.stateActionWeights.length){
				return this.stateActionWeights[i];
			}
		}
		throw new RuntimeException("Parameter index out of bounds; parameter cannot be returned.");
	}

	@Override
	public void setParameter(int i, double p) {
		if(this.stateWeights != null){
			if(i < this.stateWeights.length){
				this.stateWeights[i] = p;
			}
		}
		else if(this.stateActionWeights != null){
			if(i < this.stateActionWeights.length){
				this.stateActionWeights[i] = p;
			}
		}
		throw new RuntimeException("Parameter index out of bounds; parameter cannot be set.");
	}

	@Override
	public void resetParameters() {
		if(this.stateWeights != null){
			for(int i = 0; i < this.stateWeights.length; i++){
				this.stateWeights[i] = this.defaultWeight;
			}
		}
		else if(this.stateActionWeights != null){
			for(int i = 0; i < this.stateActionWeights.length; i++){
				this.stateActionWeights[i] = this.defaultWeight;
			}
		}
	}


	public int getActionOffset(AbstractGroundedAction a){
		Integer offset = this.actionOffset.get(a);
		if(offset == null){
			offset = this.actionOffset.size();
			this.actionOffset.put(a, offset);
			this.expandStateActionWeights(this.currentStateFeatures.length);
		}
		return offset;
	}

	/**
	 * Expands the state-action function weight vector by a fixed sized and initializes their value
	 * to the default weight value set for this object.
	 * @param num the number of function weights to add to the state-action function weight vector
	 */
	protected void expandStateActionWeights(int num){

		if(this.stateActionWeights == null){
			this.stateActionWeights = new double[num];
			for(int i = 0; i < this.stateActionWeights.length; i++){
				this.stateActionWeights[i] = this.defaultWeight;
			}
		}
		else{
			double [] nWeights = new double[this.stateActionWeights.length + num];
			for(int i = 0; i < this.stateActionWeights.length; i++){
				nWeights[i] = this.stateActionWeights[i];
			}
			for(int i = this.stateActionWeights.length; i < nWeights.length; i++){
				nWeights[i] = this.defaultWeight;
			}
			this.stateActionWeights = nWeights;
		}

	}

	/**
	 * Initializes. This object will be set to perform either state value function approximation or state-action
	 * function approximation once a call to either {@link #functionInput(burlap.oomdp.core.states.State)}
	 * or {@link #functionInput(burlap.oomdp.core.states.State, burlap.oomdp.core.AbstractGroundedAction)} is made.
	 * If the former method is called
	 * first, then this object will be tasked with state value function approximation. If the latter
	 * method is called first, then this object will be tasked with state-action value function approximation.
	 * @param fvGen The state feature vector generator that produces the features used for either linear state value function approximation or state-action-value function approximation.
	 * @param defaultWeightValue The default weight value of all function weights.
	 */
	public LinearFVVFA(StateToFeatureVectorGenerator fvGen, double defaultWeightValue){
		this.fvGen = fvGen;
		this.defaultWeight = defaultWeightValue;
	}


	public StateToFeatureVectorGenerator getFvGen() {
		return fvGen;
	}

	public double getDefaultWeight() {
		return defaultWeight;
	}



	/**
	 * Resets the state function weight array to a new array of the given sized and default value.
	 * @param size the dimensionality of the weights
	 * @param v the default value to which the weights will be set
	 */
	public void initializeStateWeightVector(int size, double v){
		this.stateWeights = new double[size];
		for(int i = 0; i < this.stateWeights.length; i++){
			this.stateWeights[i] = v;
		}
	}


	/**
	 * Resets the state-action function weight array to a new array of the given sized and default value.
	 * @param size the dimensionality of the weights
	 * @param v the default value to which the weights will be set
	 */
	public void initializeStateActionWeightVector(int size, double v){
		this.stateActionWeights = new double[size];
		for(int i = 0; i < this.stateActionWeights.length; i++){
			this.stateActionWeights[i] = v;
		}
	}




	/**
	 * Returns the {@link java.util.Map} of feature index offsets into the full feature vector for each action
	 * @return the {@link java.util.Map} of feature index offsets into the full feature vector for each action
	 */
	public Map<AbstractGroundedAction, Integer> getActionOffset() {
		return actionOffset;
	}


	/**
	 * Sets the {@link java.util.Map} of feature index offsets into the full feature vector for each action
	 * @param actionOffset the {@link java.util.Map} of feature index offsets into the full feature vector for each action
	 */
	public void setActionOffset(Map<AbstractGroundedAction, Integer> actionOffset) {
		this.actionOffset = actionOffset;
	}

	/**
	 * Sets the {@link java.util.Map} of feature index offset into the full feature vector for the given action
	 * @param a the action whose feature vector index is to be set
	 * @param offset the feature index offset for the action
	 */
	public void setActionOffset(GroundedAction a, int offset){
		this.actionOffset.put(a, offset);
	}

	@Override
	public LinearFVVFA copy() {
		LinearFVVFA vfa = new LinearFVVFA(this.fvGen, this.defaultWeight);
		vfa.actionOffset = new HashMap<AbstractGroundedAction, Integer>(this.actionOffset);

		if(this.stateWeights != null) {
			vfa.stateWeights = new double[this.stateWeights.length];
			for(int i = 0; i < this.stateWeights.length; i++) {
				vfa.stateWeights[i] = this.stateWeights[i];
			}
		}
		if(this.stateActionWeights != null) {
			vfa.stateActionWeights = new double[this.stateActionWeights.length];
			for(int i = 0; i < this.stateActionWeights.length; i++) {
				vfa.stateActionWeights[i] = this.stateActionWeights[i];
			}
		}


		return vfa;
	}
}
