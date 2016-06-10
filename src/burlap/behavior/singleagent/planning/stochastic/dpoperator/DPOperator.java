package burlap.behavior.singleagent.planning.stochastic.dpoperator;

/**
 * Defines a function for applying a dynamic programming operator (e.g., reducing the Q-values into a state value).
 * The standard approach is the {@link BellmanOperator}, which applies max operator.
 * @author James MacGlashan.
 */
public interface DPOperator {

	/**
	 * Applies the operator on the input q-values and returns the result.
	 * @param qs the q-values on which the operator is to be applied.
	 * @return the result of the operator
	 */
	double apply(double [] qs);
}
