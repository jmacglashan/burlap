package burlap.mdp.core.state.vardomain;

/**
 * A tuple specifying the numeric domain of a {@link burlap.mdp.core.state.State} variable.
 * @author James MacGlashan.
 */
public class VariableDomain {

	/**
	 * The lower value of the domain
	 */
	public double lower;

	/**
	 * The upper value of the domain
	 */
	public double upper;

	public VariableDomain() {
	}

	/**
	 * Initializes.
	 * @param lower The lower value of the domain
	 * @param upper The upper value of the domain
	 */
	public VariableDomain(double lower, double upper) {
		this.lower = lower;
		this.upper = upper;
	}

	/**
	 * Returns the spanning size of the domain; that is, upper - lower
	 * @return upper - lower
	 */
	public double span(){
		return upper - lower;
	}

	/**
	 * Given a value in this variable domain, returns its normalized value. That is,
	 * (d - lower) / (upper - lower)
	 * @param d the input value
	 * @return the normalized value
	 */
	public double norm(double d){
		return (d - lower) / this.span();
	}
}
