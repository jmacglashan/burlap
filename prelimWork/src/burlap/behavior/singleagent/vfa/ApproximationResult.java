package burlap.behavior.singleagent.vfa;

import java.util.List;


/**
 * A list associating a predicted value that was generated from a list of state features and the weights for those features. Note that
 * the predicted value does *not* have to be a linear combination of the state features and the weights, so it may not be possible
 * to reconstruct the predicted value from the features and weights alone.
 * @author James MacGlashan
 *
 */
public class ApproximationResult {

	/**
	 * The predicted valued
	 */
	public double					predictedValue;
	
	/**
	 * The state features used to produce the predicted value.
	 */
	public List<StateFeature>		stateFeatures;
	
	/**
	 * The function weights used to produce the predicted value.
	 */
	public List<FunctionWeight>		functionWeights;
	
	
	
	/**
	 * Initializes
	 * @param predictedValue the predicted value
	 * @param stateFeatures the state features used to produce the predicted value.
	 * @param functionWeights the function weights used to produce the predicted value.
	 */
	public ApproximationResult(double predictedValue, List <StateFeature> stateFeatures, List <FunctionWeight> functionWeights) {
		this.predictedValue = predictedValue;
		this.stateFeatures = stateFeatures;
		this.functionWeights = functionWeights;
		
	}

}
