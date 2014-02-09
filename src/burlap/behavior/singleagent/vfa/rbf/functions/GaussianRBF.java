package burlap.behavior.singleagent.vfa.rbf.functions;

import burlap.behavior.singleagent.vfa.rbf.DistanceMetric;
import burlap.behavior.singleagent.vfa.rbf.RBF;
import burlap.oomdp.core.State;

/**
 * An RBF whose response is dictated by a Gaussian kernel. More specifically, this RBF returns
 * e^(-1 * d(x, c)^2 / e^2), where d(x, c) is the distance from a query state s to a defined "center,"
 * c, of this RBF and e is a bandwidth parameter on (0, +infinity) that affects the range of influence of this RBF. The
 * larger the bandwidth value, the more uniform the response it gives to any arbitrary input state. The distance
 * between the query state and the center state is determined by a provided distance function.
 * @author Anubhav Malhotra and Daniel Fernandez and Spandan Dutta
 *
 */
public class GaussianRBF extends RBF {

	/**
	 * The bandwidth parameter. The
	 * larger the bandwidth value, the more uniform the response it gives to any arbitrary input state.
	 */
	protected double epsilon;
	
	/**
	 * Initializes with a center state, a distance metric and a bandwidth parameter.
	 * @param centerdState the center state of this RBF unit.
	 * @param metric the distance metric to use.
	 * @param epsilon the bandwidth parameter.
	 */
	public GaussianRBF(State centerdState, DistanceMetric metric, double epsilon)
	{
		super(centerdState,metric);
		this.epsilon = epsilon;
	}
		
	@Override
	public double responseFor(State input) 
	{
		double distance = metric.distance(centeredState, input);
		
		return Math.exp(-1 * (Math.pow( distance/ epsilon, 2.0)));

	}

}
