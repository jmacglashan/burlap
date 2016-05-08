package burlap.behavior.functionapproximation.dense.rbf.functions;

import burlap.behavior.functionapproximation.dense.DenseStateFeatures;
import burlap.behavior.functionapproximation.dense.rbf.DistanceMetric;
import burlap.behavior.functionapproximation.dense.rbf.RBF;
import burlap.behavior.functionapproximation.dense.rbf.metrics.EuclideanDistance;
import burlap.mdp.core.state.State;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link RBF} whose response is dictated by a Gaussian kernel. More specifically, this RBF returns
 * e^(-1 * d(x, c)^2 / e^2), where d(x, c) is the distance from a query state s to a defined "center,"
 * c, of this RBF and e is a bandwidth parameter on (0, +infinity) that affects the range of influence of this RBF. The
 * larger the bandwidth value, the more uniform the response it gives to any arbitrary input state. The distance
 * between the query state and the center state is determined by a provided distance function.
 * @author James MacGlashan.
 */
public class GaussianRBF extends RBF {

	/**
	 * The bandwidth parameter. The
	 * larger the bandwidth value, the more uniform the response it gives to any arbitrary input state.
	 */
	protected double epsilon;


	/**
	 * Initializes.
	 * @param centeredState the centered state for this unit represented as a double array
	 * @param metric the distance metric with which to compare states
	 * @param epsilon the Gaussian bandwidth value; the larger the value, the more uniform a response this unit gives
	 */
	public GaussianRBF(double[] centeredState, DistanceMetric metric, double epsilon) {
		super(centeredState, metric);
		this.epsilon = epsilon;
	}

	/**
	 * Initializes using an {@link EuclideanDistance} distance metric.
	 * @param centeredState the centered state for this unit represented as a double array
	 * @param epsilon the Gaussian bandwidth value; the larger the value, the more uniform a response this unit gives
	 */
	public GaussianRBF(double[] centeredState, double epsilon) {
		super(centeredState, new EuclideanDistance());
		this.epsilon = epsilon;
	}


	@Override
	public double responseFor(double[] input) {
		double distance = metric.distance(centeredState, input);

		return Math.exp(-1 * (Math.pow( distance/ epsilon, 2.0)));
	}




	/**
	 * Creates a {@link java.util.List} of {@link GaussianRBF} units
	 * for each {@link State} provided using the given {@link DenseStateFeatures}, metric, and epsilon value.
	 * @param states the {@link State} objects around which a {@link burlap.behavior.functionapproximation.dense.rbf.functions.GaussianRBF} will be created
	 * @param fvGen the {@link DenseStateFeatures} used to convert states to a double array usable by {@link RBF} units.
	 * @param metric the {@link burlap.behavior.functionapproximation.dense.rbf.DistanceMetric} to use
	 * @param epsilon the bandwidth parameter.
	 * @return a {@link java.util.List} of {@link burlap.behavior.functionapproximation.dense.rbf.functions.GaussianRBF} units.
	 */
	public static List<RBF> generateGaussianRBFsForStates(List<State> states, DenseStateFeatures fvGen, DistanceMetric metric, double epsilon){


		List<RBF> units = new ArrayList<RBF>(states.size());
		for(State s : states){
			units.add(new GaussianRBF(fvGen.generateFeatureVectorFrom(s), metric, epsilon));
		}

		return units;
	}


	/**
	 * Creates a {@link java.util.List} of {@link GaussianRBF} units
	 * for each {@link State} provided using the given {@link DenseStateFeatures}, and epsilon value
	 * and using a default {@link EuclideanDistance} metric for all units.
	 * @param states the {@link State} objects around which a {@link burlap.behavior.functionapproximation.dense.rbf.functions.GaussianRBF} will be created
	 * @param fvGen the {@link DenseStateFeatures} used to convert states to a double array usable by {@link RBF} units.
	 * @param epsilon the bandwidth parameter.
	 * @return a {@link java.util.List} of {@link burlap.behavior.functionapproximation.dense.rbf.functions.GaussianRBF} units.
	 */
	public static List<RBF> generateGaussianRBFsForStates(List<State> states, DenseStateFeatures fvGen, double epsilon){


		List<RBF> units = new ArrayList<RBF>(states.size());
		for(State s : states){
			units.add(new GaussianRBF(fvGen.generateFeatureVectorFrom(s), epsilon));
		}

		return units;
	}



}
