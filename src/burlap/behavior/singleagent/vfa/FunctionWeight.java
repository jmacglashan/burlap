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

	protected int weightId;
	protected double weightValue;
	
	public FunctionWeight(int weightId, double weightValue) {
		this.weightId = weightId;
		this.weightValue = weightValue;
	}
	
	public int weightId(){
		return this.weightId;
	}
	
	public double weightValue(){
		return weightValue;
	}
	
	public void setWeight(double w){
		this.weightValue = w;
	}
	

}
