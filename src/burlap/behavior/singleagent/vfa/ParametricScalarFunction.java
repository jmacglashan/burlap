package burlap.behavior.singleagent.vfa;

/**
 * An interface for defining a parametric function that outputs a scalar values (double). This interface is often used
 * for value function approximation implementations. This highest-level of the interface does not define the input
 * domain, which is left to subclasses of this interface. For example, {@link burlap.behavior.singleagent.vfa.DifferentiableStateValue}
 * defines a function that operates on state inputs and {@link burlap.behavior.singleagent.vfa.DifferentiableStateActionValue}
 * defines a function that operates on state-action inputs.
 * @author James MacGlashan.
 */
public interface ParametricScalarFunction {

	/**
	 * Returns the value of this function for the last input values.
	 * @return the value of this function for the last input values.
	 */
	double functionValue();

	/**
	 * Returns the number of parameters defining this function. Note that some
	 * implementations my have a dynamic number of parameters that grows or shrinks
	 * over time. Consult the documentation for the specific implementation
	 * for more information.
	 * @return the number of parameters defining this function.
	 */
	int numParameters();


	/**
	 * Returns the value of the ith parameter value
	 * @param i the parameter index
	 * @return the double value of the ith parameter
	 */
	double getParameter(int i);


	/**
	 * Sets the value of the ith parameter to given value
	 * @param i the index of the parameter to set
	 * @param p the parameter value to which it should be set
	 */
	void setParameter(int i, double p);


	/**
	 * Resets the parameters of this function to default values.
	 */
	void resetParameters();


	/**
	 * Returns a copy of this {@link burlap.behavior.singleagent.vfa.ParametricScalarFunction}.
	 * @return a copy of this {@link burlap.behavior.singleagent.vfa.ParametricScalarFunction}.
	 */
	ParametricScalarFunction copy();

}
