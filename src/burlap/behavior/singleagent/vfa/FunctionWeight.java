package burlap.behavior.singleagent.vfa;

/**
 * This class holds the weight value for weights defined by a ValueFunctionApproximation class. It is expected that when a weight value is changed
 * on this object that the corresponding weight value in the ValueFunctionApproximation object is changed as well, which means the
 * ValueFunctionApproximation should store its weights with this data structure. Alternative, the ValueFunctionApproximation class
 * can use a different data structure and subclass this FunctionWeight class so that when the setWeight method is called on it, it also
 * updates the corresponding data structure in the ValueFunctionApproximation object. 
 * @author James MacGlashan
 *
 */
public class FunctionWeight {

	/**
	 * The int value that uniquely identifies this weight
	 */
	protected int weightId;
	
	/**
	 * The value of this weight.
	 */
	protected double weightValue;
	
	
	/**
	 * Initializes.
	 * @param weightId the weight identifier
	 * @param weightValue the value of the weight
	 */
	public FunctionWeight(int weightId, double weightValue) {
		this.weightId = weightId;
		this.weightValue = weightValue;
	}
	
	
	/**
	 * Returns the weight identifier
	 * @return the weight identifier
	 */
	public int weightId(){
		return this.weightId;
	}
	
	
	/**
	 * Returns the weight value
	 * @return the weight value
	 */
	public double weightValue(){
		return weightValue;
	}
	
	
	/**
	 * Sets the weight
	 * @param w the value to set the weight to
	 */
	public void setWeight(double w){
		this.weightValue = w;
	}
	

}
