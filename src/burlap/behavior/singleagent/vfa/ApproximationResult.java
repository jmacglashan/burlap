package burlap.behavior.singleagent.vfa;

import java.util.List;

public class ApproximationResult {

	public double					predictedValue;
	public List<StateFeature>		stateFeatures;
	public List<FunctionWeight>		functionWeights;
	
	
	public ApproximationResult(double predictedValue, List <StateFeature> stateFeatures, List <FunctionWeight> functionWeights) {
		this.predictedValue = predictedValue;
		this.stateFeatures = stateFeatures;
		this.functionWeights = functionWeights;
		
	}

}
