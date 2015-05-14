package burlap.behavior.singleagent.vfa.rbf.functions;

import burlap.behavior.singleagent.vfa.StateToFeatureVectorGenerator;
import burlap.behavior.singleagent.vfa.rbf.FVDistanceMetric;
import burlap.behavior.singleagent.vfa.rbf.FVRBF;
import burlap.oomdp.core.State;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link burlap.behavior.singleagent.vfa.rbf.FVRBF} whose response is dictated by a Gaussian kernel. More specifically, this RBF returns
 * e^(-1 * d(x, c)^2 / e^2), where d(x, c) is the distance from a query state s to a defined "center,"
 * c, of this RBF and e is a bandwidth parameter on (0, +infinity) that affects the range of influence of this RBF. The
 * larger the bandwidth value, the more uniform the response it gives to any arbitrary input state. The distance
 * between the query state and the center state is determined by a provided distance function.
 * @author James MacGlashan.
 */
public class FVGaussianRBF extends FVRBF {

	/**
	 * The bandwidth parameter. The
	 * larger the bandwidth value, the more uniform the response it gives to any arbitrary input state.
	 */
	protected double epsilon;

	public FVGaussianRBF(double[] centeredState, FVDistanceMetric metric, double epsilon) {
		super(centeredState, metric);
		this.epsilon = epsilon;
	}


	@Override
	public double responseFor(double[] input) {
		double distance = metric.distance(centeredState, input);

		return Math.exp(-1 * (Math.pow( distance/ epsilon, 2.0)));
	}




	/**
	 * Creates a {@link java.util.List} of {@link burlap.behavior.singleagent.vfa.rbf.functions.FVGaussianRBF} units
	 * for each {@link burlap.oomdp.core.State} provided using the given {@link burlap.behavior.singleagent.vfa.StateToFeatureVectorGenerator}, metric, and epsilon value.
	 * @param states the {@link burlap.oomdp.core.State} objects around which a {@link burlap.behavior.singleagent.vfa.rbf.functions.GaussianRBF} will be created
	 * @param fvGen the {@link burlap.behavior.singleagent.vfa.StateToFeatureVectorGenerator} used to convert states to a double array usable by {@link burlap.behavior.singleagent.vfa.rbf.FVRBF} units.
	 * @param metric the {@link burlap.behavior.singleagent.vfa.rbf.DistanceMetric} to use
	 * @param epsilon the bandwidth parameter.
	 * @return a {@link java.util.List} of {@link burlap.behavior.singleagent.vfa.rbf.functions.GaussianRBF} units.
	 */
	public static List<FVRBF> generateGaussianRBFsForStates(List<State> states, StateToFeatureVectorGenerator fvGen, FVDistanceMetric metric, double epsilon){


		List<FVRBF> units = new ArrayList<FVRBF>(states.size());
		for(State s : states){
			units.add(new FVGaussianRBF(fvGen.generateFeatureVectorFrom(s), metric, epsilon));
		}

		return units;
	}



}
