package burlap.mdp.singleagent.pomdp.beliefstate;

/**
 * An interface to be used in conjunction with {@link burlap.mdp.singleagent.pomdp.beliefstate.BeliefState} instances
 * for belief states that can generate a dense belief vector representation.
 * @author James MacGlashan.
 */
public interface DenseBeliefVector extends EnumerableBeliefState{

	/**
	 * Returns a dense belief vector representation of the this belief state.
	 * @return a double array specifying the dense belief vector representation.
	 */
	double [] beliefVector();

	/**
	 * Sets this belief state to the provided. Dense belief vector. If the belief vector dimensionality does not match
	 * this objects dimensionality then a runtime exception will be thrown.
	 * @param b the belief vector to set this belief state to.
	 */
	void setBeliefVector(double [] b);
}
