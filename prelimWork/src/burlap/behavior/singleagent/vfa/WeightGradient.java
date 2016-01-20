package burlap.behavior.singleagent.vfa;

import java.util.HashMap;
import java.util.Map;


/**
 * A data structure for defining the gradient of the weights for a vector. If the weight gradient is not stored for a given
 * feature, then zero will be returned.
 * @author James MacGlashan
 *
 */
public class WeightGradient {

	/**
	 * A map from weight identifiers to their partial derivative
	 */
	Map<Integer, Double> gradient;
	
	
	/**
	 * Initializes with the gradient unspecified for any weights.
	 */
	public WeightGradient() {
		gradient = new HashMap<Integer, Double>();
	}
	
	
	/**
	 * Initializes with the gradient unspecified, but reserves space for the given capacity
	 * @param capacity how much space to reserve for storing the gradient; i.e., the number of weights over which the gradient will be defined
	 */
	public WeightGradient(int capacity) {
		gradient = new HashMap<Integer, Double>(capacity);
	}
	
	/**
	 * Adds the partial derivative for a given weight
	 * @param weightId the weight identifier for which the partial derivative is to be stored is to be stored
	 * @param partialDerivative the partial derivative value for the weight
	 */
	public void put(int weightId, double partialDerivative){
		this.gradient.put(weightId, partialDerivative);
	}
	
	
	/**
	 * Returns the partial derivative for the given weight
	 * @param weightId
	 * @return the partial derivative for the given weight; 0 if it is not stored.
	 */
	public double getPartialDerivative(int weightId){
		Double stored = gradient.get(weightId);
		if(stored == null){
			return 0.;
		}
		return stored;
	}

}
