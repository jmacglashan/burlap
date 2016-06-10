package burlap.behavior.functionapproximation.dense.rbf;

/**
 * An interface for defining the distance between two states that are represented with double arrays.
 * @author James MacGlashan.
 */
public interface DistanceMetric {

	/**
	 * Returns the distance between state s0 and state s1.
	 * @param s0 a state represented with a double array
	 * @param s1 a state represented with a double array
	 * @return the distance between s0 and s1.
	 */
	public double distance(double [] s0, double [] s1);
}
