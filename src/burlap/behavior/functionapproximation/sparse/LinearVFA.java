package burlap.behavior.functionapproximation.sparse;

import burlap.behavior.functionapproximation.*;
import burlap.mdp.core.AbstractGroundedAction;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.GroundedAction;

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
	protected SparseStateFeatures sparseStateFeatures;
	
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
	protected FunctionGradient currentGradient = null;

	protected State									lastState = null;
	protected AbstractGroundedAction				lastAction = null;


	/**
	 * Initializes with a feature database; the default weight value will be zero
	 * @param sparseStateFeatures the feature database to use
	 */
	public LinearVFA(SparseStateFeatures sparseStateFeatures) {

		this.sparseStateFeatures = sparseStateFeatures;
		if(sparseStateFeatures.numberOfFeatures() > 0){
			this.weights = new HashMap<Integer, Double>(sparseStateFeatures.numberOfFeatures());
		}
		else{
			this.weights = new HashMap<Integer, Double>();
		}

	}


	/**
	 * Initializes
	 * @param sparseStateFeatures the feature database to use
	 * @param defaultWeight the default feature weight to initialize feature weights to
	 */
	public LinearVFA(SparseStateFeatures sparseStateFeatures, double defaultWeight) {

		this.sparseStateFeatures = sparseStateFeatures;
		this.defaultWeight = defaultWeight;
		if(sparseStateFeatures.numberOfFeatures() > 0){
			this.weights = new HashMap<Integer, Double>(sparseStateFeatures.numberOfFeatures());
		}
		else{
			this.weights = new HashMap<Integer, Double>();
		}

	}



	@Override
	public double evaluate(State s, AbstractGroundedAction a) {

		List<StateFeature> features = this.sparseStateFeatures.getActionFeaturesSets(s, Arrays.asList((GroundedAction)a)).get(0).features;
		double val = 0.;
		for(StateFeature sf : features){
			double prod = sf.value * this.getWeight(sf.id);
			val += prod;
		}
		this.currentValue = val;
		this.currentGradient = null;
		this.currentFeatures = features;
		this.lastState = s;
		this.lastAction = a;
		return val;
	}

	@Override
	public double evaluate(State s) {
		List<StateFeature> features = this.sparseStateFeatures.features(s);
		double val = 0.;
		for(StateFeature sf : features){
			double prod = sf.value * this.getWeight(sf.id);
			val += prod;
		}
		this.currentValue = val;
		this.currentGradient = null;
		this.currentFeatures = features;
		this.lastState = s;
		this.lastAction = null;
		return this.currentValue;
	}


	@Override
	public FunctionGradient gradient(State s) {

		List<StateFeature> features;

		if(this.lastState == s && this.lastAction == null){
			if(this.currentGradient != null) {
				return this.currentGradient;
			}
			features = this.currentFeatures;
		}
		else{
			features = this.sparseStateFeatures.features(s);
		}

		FunctionGradient gd = new FunctionGradient.SparseGradient(features.size());
		for(StateFeature sf : features){
			gd.put(sf.id, sf.value);
		}
		this.currentGradient = gd;
		this.lastState = s;
		this.lastAction = null;
		this.currentFeatures = features;

		return gd;
	}

	@Override
	public FunctionGradient gradient(State s, AbstractGroundedAction a) {

		List<StateFeature> features;

		if(this.lastState == s && this.lastAction == a){
			if(this.currentGradient != null) {
				return this.currentGradient;
			}
			features = this.currentFeatures;
		}
		else{
			features = this.sparseStateFeatures.getActionFeaturesSets(s, Arrays.asList((GroundedAction)a)).get(0).features;
		}

		FunctionGradient gd = new FunctionGradient.SparseGradient(features.size());
		for(StateFeature sf : features){
			gd.put(sf.id, sf.value);
		}
		this.currentGradient = gd;
		this.lastState = s;
		this.lastAction = a;
		this.currentFeatures = features;

		return gd;
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

		LinearVFA vfa = new LinearVFA(this.sparseStateFeatures.copy(), this.defaultWeight);
		vfa.weights = new HashMap<Integer, Double>(this.weights.size());
		for(Map.Entry<Integer, Double> e : this.weights.entrySet()){
			vfa.weights.put(e.getKey(), e.getValue());
		}

		return vfa;
	}

}
