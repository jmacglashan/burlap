package burlap.mdp.core.state.range;

/**
 * @author James MacGlashan.
 */
public class VariableDomain {
	public double lower;
	public double upper;

	public VariableDomain() {
	}

	public VariableDomain(double lower, double upper) {
		this.lower = lower;
		this.upper = upper;
	}

	public double span(){
		return upper - lower;
	}

	public double norm(double d){
		return (d - lower) / this.span();
	}
}
