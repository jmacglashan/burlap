package burlap.behavior.singleagent.vfa;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * A data structure for defining the gradient of a function. This data structure only explicitly stores
 * the partial derivative for parameters with non-zero partial derivatives. The partial derivative for parameters
 * not explicitly stored will have a value of zero returned.
 * @author James MacGlashan
 *
 */
public class FunctionGradient {

	/**
	 * A map from weight identifiers to their partial derivative
	 */
	protected Map<Integer, Double> gradient;
	
	
	/**
	 * Initializes with the gradient unspecified for any weights.
	 */
	public FunctionGradient() {
		gradient = new HashMap<Integer, Double>();
	}
	
	
	/**
	 * Initializes with the gradient unspecified, but reserves space for the given capacity
	 * @param capacity how much space to reserve for storing the gradient; i.e., the number of weights over which the gradient will be defined
	 */
	public FunctionGradient(int capacity) {
		gradient = new HashMap<Integer, Double>(capacity);
	}
	
	/**
	 * Adds the partial derivative for a given weight
	 * @param parameterId the parameter identifier for which the partial derivative is to be stored.
	 * @param partialDerivative the partial derivative value for the parameter
	 */
	public void put(int parameterId, double partialDerivative){
		if(partialDerivative == 0.){
			if(this.gradient.containsKey(parameterId)){
				this.gradient.remove(parameterId);
			}
		}
		else {
			this.gradient.put(parameterId, partialDerivative);
		}
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

	/**
	 * Returns all non-zero partial derivatives. Result is stored in a {@link java.util.Map.Entry} where
	 * the key is an Integer of the parameter identifier and the value is a double specifying its partial
	 * derivative.
	 * @return all non-zero partial derivatives.
	 */
	public Set<Map.Entry<Integer, Double>> getNonZeroPartialDerivatives(){
		return this.gradient.entrySet();
	}


	/**
	 * Returns the number of non-zero partial derivatives
	 * @return the number of non-zero partial derivatives
	 */
	public int numNonZeroPDs(){
		return this.gradient.size();
	}

}
