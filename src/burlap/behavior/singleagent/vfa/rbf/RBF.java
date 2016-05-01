package burlap.behavior.singleagent.vfa.rbf;

/**
 * A class for defining radial basis functions for states represented with a double array.
 * @author James MacGlashan.
 */
public abstract class RBF {

	/**
	 * The center state of the RBF unit.
	 */
	protected double [] centeredState;

	/**
	 * The distance metric to compare query input states to the centeredState
	 */
	protected DistanceMetric metric;


	/**
	 * Initializes.
	 * @param centeredState the center state of the RBF unit.
	 * @param metric the distance metric to compare query input states to the centeredState
	 */
	public RBF(double [] centeredState, DistanceMetric metric){
		this.centeredState = centeredState;
		this.metric = metric;
	}

	/**
	 * Returns the RBF response from its center state to the query input state.
	 * @param input the query input state represented with a double array.
	 * @return the double response value of this RBF unit to the query input state.
	 */
	public abstract double responseFor(double [] input);

}
