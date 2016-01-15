package burlap.behavior.singleagent.vfa.common;

import burlap.behavior.singleagent.vfa.*;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This class is used for general purpose linear VFA. It only needs to be provided a FeatureDatabase object that will be used to store
 * retrieve state features. For every feature returned by the feature database, this class will automatically create a weight associated with it.
 * The returned approximated value for any state is the linear combination of state features and weights.
 *  
 * @author James MacGlashan
 *
 */
public class LinearVFA implements DifferentiableStateValue, DifferentiableStateActionValue {

	/**
	 * A feature database for which a unique function weight will be associated
	 */
	protected FeatureDatabase						featureDatabase;
	
	/**
	 * A map from feature identifiers to function weights
	 */
	protected Map<Integer, Double>					weights;
	
	/**
	 * A default weight for the functions
	 */
	protected double								defaultWeight = 0.0;




	protected List<StateFeature>					currentFeatures;
	protected double								currentValue;
	protected FunctionGradient						currentGradient = null;


	/**
	 * Initializes with a feature database; the default weight value will be zero
	 * @param featureDatabase the feature database to use
	 */
	public LinearVFA(FeatureDatabase featureDatabase) {

		this.featureDatabase = featureDatabase;
		if(featureDatabase.numberOfFeatures() > 0){
			this.weights = new HashMap<Integer, Double>(featureDatabase.numberOfFeatures());
		}
		else{
			this.weights = new HashMap<Integer, Double>();
		}

	}


	/**
	 * Initializes
	 * @param featureDatabase the feature database to use
	 * @param defaultWeight the default feature weight to initialize feature weights to
	 */
	public LinearVFA(FeatureDatabase featureDatabase, double defaultWeight) {

		this.featureDatabase = featureDatabase;
		this.defaultWeight = defaultWeight;
		if(featureDatabase.numberOfFeatures() > 0){
			this.weights = new HashMap<Integer, Double>(featureDatabase.numberOfFeatures());
		}
		else{
			this.weights = new HashMap<Integer, Double>();
		}

	}



	@Override
	public double functionInput(State s, AbstractGroundedAction a) {
		this.currentFeatures = this.featureDatabase.getActionFeaturesSets(s, Arrays.asList((GroundedAction)a)).get(0).features;
		double val = 0.;
		for(StateFeature sf : this.currentFeatures){
			double prod = sf.value * this.getWeight(sf.id);
			val += prod;
		}
		this.currentValue = val;
		this.currentGradient = null;
		return this.currentValue;
	}

	@Override
	public double functionInput(State s) {
		this.currentFeatures = this.featureDatabase.getStateFeatures(s);
		double val = 0.;
		for(StateFeature sf : this.currentFeatures){
			double prod = sf.value * this.getWeight(sf.id);
			val += prod;
		}
		this.currentValue = val;
		this.currentGradient = null;
		return this.currentValue;
	}

	@Override
	public FunctionGradient computeGradient() {

		if(this.currentFeatures == null){
			throw new RuntimeException("Input has not been set for this function; cannot return a gradient.");
		}

		if(this.currentGradient != null){
			return this.currentGradient;
		}
		FunctionGradient gd = new FunctionGradient(this.currentFeatures.size());
		for(StateFeature sf : this.currentFeatures){
			gd.put(sf.id, sf.value);
		}
		this.currentGradient = gd;

		return this.currentGradient;
	}

	@Override
	public double functionValue() {
		if(this.currentFeatures == null){
			throw new RuntimeException("Input has not been set for this function; cannot return a value.");
		}
		return this.currentValue;
	}

	@Override
	public int numParameters() {
		return this.weights.size();
	}

	@Override
	public double getParameter(int i) {
		return this.getWeight(i);
	}

	@Override
	public void setParameter(int i, double p) {
		this.weights.put(i, p);
	}

	protected double getWeight(int weightId){
		Double stored = this.weights.get(weightId);
		if(stored == null){
			this.weights.put(weightId, this.defaultWeight);
			return this.defaultWeight;
		}
		return stored;
	}


	@Override
	public void resetParameters() {
		this.weights.clear();
	}

	@Override
	public LinearVFA copy() {

		LinearVFA vfa = new LinearVFA(this.featureDatabase.copy(), this.defaultWeight);
		vfa.weights = new HashMap<Integer, Double>(this.weights.size());
		for(Map.Entry<Integer, Double> e : this.weights.entrySet()){
			vfa.weights.put(e.getKey(), e.getValue());
		}

		return vfa;
	}

}
