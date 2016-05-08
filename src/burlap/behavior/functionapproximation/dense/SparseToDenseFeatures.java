package burlap.behavior.functionapproximation.dense;

import burlap.behavior.functionapproximation.sparse.SparseStateFeatures;
import burlap.behavior.functionapproximation.sparse.StateFeature;
import burlap.mdp.core.state.State;

import java.util.List;

/**
 * A wrapper for turning the features from a {@link SparseStateFeatures} into a double array.
 * Note that this wrapper is not advised for feature databases like CMACs/Tile coding, since those have very large numbers
 * of sparse features and this wrapper will create entries for all features, including the zero-valued ones.
 * @author James MacGlashan.
 */
public class SparseToDenseFeatures implements DenseStateFeatures {

	protected SparseStateFeatures fd;


	/**
	 * Initializes.
	 * @param fd the feature database used for generating state features.
	 */
	public SparseToDenseFeatures(SparseStateFeatures fd){
		this.fd = fd;
	}

	public SparseStateFeatures getFd() {
		return fd;
	}

	public void setFd(SparseStateFeatures fd) {
		this.fd = fd;
	}

	@Override
	public double[] features(State s) {

		List<StateFeature> sfs = this.fd.features(s);
		double [] fv = new double[this.fd.numberOfFeatures()];
		for(StateFeature sf : sfs){
			fv[sf.id] = sf.value;
		}
		return fv;
	}
}
