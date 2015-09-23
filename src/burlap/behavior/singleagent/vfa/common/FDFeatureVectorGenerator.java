package burlap.behavior.singleagent.vfa.common;

import burlap.behavior.singleagent.vfa.FeatureDatabase;
import burlap.behavior.singleagent.vfa.StateFeature;
import burlap.behavior.singleagent.vfa.StateToFeatureVectorGenerator;
import burlap.oomdp.core.states.State;

import java.util.List;

/**
 * A wrapper for turning the features from a {@link burlap.behavior.singleagent.vfa.FeatureDatabase} into a double array.
 * Note that this wrapper is not advised for feature databases like CMACs/Tile coding, since those have very large numbers
 * of sparse features and this wrapper will create entries for all features, including the zero-valued ones.
 * @author James MacGlashan.
 */
public class FDFeatureVectorGenerator implements StateToFeatureVectorGenerator{

	protected FeatureDatabase fd;


	/**
	 * Initializes.
	 * @param fd the feature database used for generating state features.
	 */
	public FDFeatureVectorGenerator(FeatureDatabase fd){
		this.fd = fd;
	}

	public FeatureDatabase getFd() {
		return fd;
	}

	public void setFd(FeatureDatabase fd) {
		this.fd = fd;
	}

	@Override
	public double[] generateFeatureVectorFrom(State s) {

		List<StateFeature> sfs = this.fd.getStateFeatures(s);
		double [] fv = new double[this.fd.numberOfFeatures()];
		for(StateFeature sf : sfs){
			fv[sf.id] = sf.value;
		}
		return fv;
	}
}
