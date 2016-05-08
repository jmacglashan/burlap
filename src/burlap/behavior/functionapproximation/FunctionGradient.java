package burlap.behavior.functionapproximation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * An interface for defining and querying the gradient of a function.
 * @author James MacGlashan
 *
 */
public interface FunctionGradient {


	/**
	 * Adds the partial derivative for a given weight
	 * @param parameterId the parameter identifier for which the partial derivative is to be stored.
	 * @param partialDerivative the partial derivative value for the parameter
	 */
	void put(int parameterId, double partialDerivative);
	
	
	/**
	 * Returns the partial derivative for the given weight
	 * @param parameterId the parameter for which the partial derivative will be returned
	 * @return the partial derivative for the given weight; 0 if it is not stored.
	 */
	double getPartialDerivative(int parameterId);

	/**
	 * Returns all non-zero partial derivatives. Result is stored in a {@link java.util.Map.Entry} where
	 * the key is an Integer of the parameter identifier and the value is a double specifying its partial
	 * derivative.
	 * @return all non-zero partial derivatives.
	 */
	Set<PartialDerivative> getNonZeroPartialDerivatives();


	/**
	 * Returns the number of non-zero partial derivatives
	 * @return the number of non-zero partial derivatives
	 */
	int numNonZeroPDs();


	/**
	 * A class for storing a partial derivative. Consists of the parameter id and the value of the partial derivative.
	 */
	public static class PartialDerivative{
		public int parameterId;
		public double value;

		public PartialDerivative(int parameterId, double value) {
			this.parameterId = parameterId;
			this.value = value;
		}

		@Override
		public boolean equals(Object obj) {

			if(!(obj instanceof PartialDerivative)){
				return false;
			}

			PartialDerivative o = (PartialDerivative)obj;

			return this.parameterId == o.parameterId && Double.compare(this.value, o.value) == 0;
		}

		@Override
		public int hashCode() {
			return parameterId;
		}
	}


	/**
	 * A sparse {@link FunctionGradient} that only explicitly stores
	 * the partial derivative for parameters with non-zero partial derivatives. The partial derivative for parameters
	 * not explicitly stored will have a value of zero returned.
	 */
	public static class SparseGradient implements FunctionGradient{


		/**
		 * A map from weight identifiers to their partial derivative
		 */
		protected Map<Integer, Double> gradient;


		/**
		 * Initializes with the gradient unspecified for any weights.
		 */
		public SparseGradient() {
			gradient = new HashMap<Integer, Double>();
		}


		/**
		 * Initializes with the gradient unspecified, but reserves space for the given capacity
		 * @param capacity how much space to reserve for storing the gradient; i.e., the number of weights over which the gradient will be defined
		 */
		public SparseGradient(int capacity) {
			gradient = new HashMap<Integer, Double>(capacity);
		}


		@Override
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



		@Override
		public double getPartialDerivative(int parameterId){
			Double stored = gradient.get(parameterId);
			if(stored == null){
				return 0.;
			}
			return stored;
		}

		@Override
		public Set<PartialDerivative> getNonZeroPartialDerivatives(){
			Set<PartialDerivative> nzPds = new HashSet<PartialDerivative>(this.gradient.size());
			for(Map.Entry<Integer, Double> e : this.gradient.entrySet()){
				nzPds.add(new PartialDerivative(e.getKey(), e.getValue()));
			}
			return nzPds;
		}


		@Override
		public int numNonZeroPDs(){
			return this.gradient.size();
		}



	}

}
