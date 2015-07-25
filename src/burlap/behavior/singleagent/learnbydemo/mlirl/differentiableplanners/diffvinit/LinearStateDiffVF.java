package burlap.behavior.singleagent.learnbydemo.mlirl.differentiableplanners.diffvinit;

import burlap.behavior.singleagent.vfa.StateToFeatureVectorGenerator;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.states.State;

/**
 * A class for defining a (differentiable) linear function over state features for value function initialization. This class is useful
 * for learning the value function initialization for leaf nodes of a finite horizon valueFunction with {@link burlap.behavior.singleagent.learnbydemo.mlirl.MLIRL}.
 * @author James MacGlashan.
 */
public class LinearStateDiffVF extends DifferentiableVInit.ParamedDiffVInit {


	/**
	 * The state feature vector generator over which the linear function operates
	 */
	protected StateToFeatureVectorGenerator fvgen;


	/**
	 * Initializes with the state feature vector generator over which the linear function is defined and the dimensionality of it.
	 * @param fvgen the state feature vector generator over which the linear function is defined.
	 * @param dim the dimensionality of the feature vector/parameters
	 */
	public LinearStateDiffVF(StateToFeatureVectorGenerator fvgen, int dim){
		this.dim = dim;
		this.parameters = new double[dim];
		this.fvgen = fvgen;
	}

	@Override
	public double[] getVGradient(State s) {
		return this.fvgen.generateFeatureVectorFrom(s);
	}

	@Override
	public double[] getQGradient(State s, AbstractGroundedAction ga) {
		return this.fvgen.generateFeatureVectorFrom(s);
	}

	@Override
	public double value(State s) {

		double [] features = this.fvgen.generateFeatureVectorFrom(s);

		double sum = 0.;
		for(int i = 0; i < features.length; i++){
			sum += features[i] * this.parameters[i];
		}
		return sum;
	}

	@Override
	public double qValue(State s, AbstractGroundedAction a) {
		return this.value(s);
	}

}
