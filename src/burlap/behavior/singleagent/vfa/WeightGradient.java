package burlap.behavior.singleagent.vfa;

import java.util.HashMap;
import java.util.Map;

public class WeightGradient {

	Map<Integer, Double> gradient;
	
	public WeightGradient() {
		gradient = new HashMap<Integer, Double>();
	}
	
	public WeightGradient(int capacity) {
		gradient = new HashMap<Integer, Double>(capacity);
	}
	
	public void put(int featureId, double partialDerivative){
		this.gradient.put(featureId, partialDerivative);
	}
	
	public double getPartialDerivative(int featureId){
		Double stored = gradient.get(featureId);
		if(stored == null){
			return 0.;
		}
		return stored;
	}

}
