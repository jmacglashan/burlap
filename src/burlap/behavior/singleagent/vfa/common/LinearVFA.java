package burlap.behavior.singleagent.vfa.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.behavior.singleagent.vfa.ActionApproximationResult;
import burlap.behavior.singleagent.vfa.ActionFeaturesQuery;
import burlap.behavior.singleagent.vfa.ApproximationResult;
import burlap.behavior.singleagent.vfa.FeatureDatabase;
import burlap.behavior.singleagent.vfa.FunctionWeight;
import burlap.behavior.singleagent.vfa.StateFeature;
import burlap.behavior.singleagent.vfa.ValueFunctionApproximation;
import burlap.behavior.singleagent.vfa.WeightGradient;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;



/**
 * This class is used for general purpose linear VFA. It only needs to be provided a FeatureDatabase object that will be used to store
 * retrieve state features. For every feature returned by the feature database, this class will automatically create a weight associated with it.
 * The returned approximated value for any state is the linear combination of state features and weights.
 *  
 * @author James MacGlashan
 *
 */
public class LinearVFA implements ValueFunctionApproximation {

	/**
	 * A feature database for which a unique function weight will be associated
	 */
	protected FeatureDatabase						featureDatabase;
	
	/**
	 * A map from feature identifiers to function weights
	 */
	protected Map<Integer, FunctionWeight>			weights;
	
	/**
	 * A default weight for the functions
	 */
	protected double								defaultWeight = 0.0;
	
	
	/**
	 * Initializes with a feature database; the default weight value will be zero
	 * @param featureDatabase the feature database to use
	 */
	public LinearVFA(FeatureDatabase featureDatabase) {
		
		this.featureDatabase = featureDatabase;
		this.weights = new HashMap<Integer, FunctionWeight>();
		
	}
	
	
	/**
	 * Initializes
	 * @param featureDatabase the feature database to use
	 * @param defaultWeight the default feature weight to initialize feature weights to
	 */
	public LinearVFA(FeatureDatabase featureDatabase, double defaultWeight) {
		
		this.featureDatabase = featureDatabase;
		this.defaultWeight = defaultWeight;
		this.weights = new HashMap<Integer, FunctionWeight>();
		
	}

	@Override
	public ApproximationResult getStateValue(State s) {
		
		List <StateFeature> features = featureDatabase.getStateFeatures(s);
		return this.getApproximationResultFrom(features);
	}

	@Override
	public List<ActionApproximationResult> getStateActionValues(State s, List<GroundedAction> gas) {
	
		List <ActionFeaturesQuery> featureSets = this.featureDatabase.getActionFeaturesSets(s, gas);
		List <ActionApproximationResult> results = new ArrayList<ActionApproximationResult>(featureSets.size());
		
		for(ActionFeaturesQuery afq : featureSets){
			
			ApproximationResult r = this.getApproximationResultFrom(afq.features);
			ActionApproximationResult aar = new ActionApproximationResult(afq.queryAction, r);
			results.add(aar);
			
		}
		
		return results;
	}

	@Override
	public WeightGradient getWeightGradient(ApproximationResult approximationResult) {
		
		WeightGradient gradient = new WeightGradient(approximationResult.stateFeatures.size());
		for(StateFeature sf : approximationResult.stateFeatures){
			gradient.put(sf.id, sf.value);
		}
		
		return gradient;
	}
	
	
	
	/**
	 * Computes the linear function over the given features and the stored feature weights.
	 * @param features
	 * @return
	 */
	protected ApproximationResult getApproximationResultFrom(List <StateFeature> features){
		
		List <FunctionWeight> activedWeights = new ArrayList<FunctionWeight>(features.size());
		
		double predictedValue = 0.;
		for(StateFeature sf : features){
			FunctionWeight fw = this.weights.get(sf.id);
			if(fw == null){
				fw = new FunctionWeight(sf.id, defaultWeight);
				this.weights.put(fw.weightId(), fw);
			}
			predictedValue += sf.value*fw.weightValue();
			activedWeights.add(fw);
		}
		
		ApproximationResult result = new ApproximationResult(predictedValue, features, activedWeights);
		
		return result;
		
	}

}
