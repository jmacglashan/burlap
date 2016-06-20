package burlap.behavior.functionapproximation.dense;

import burlap.behavior.functionapproximation.sparse.SparseStateFeatures;
import burlap.behavior.functionapproximation.sparse.StateFeature;
import burlap.mdp.core.state.State;

import java.util.List;

/**
 * A wrapper for turning the features from a {@link SparseStateFeatures} into a {@link DenseStateFeatures}.
 * @author James MacGlashan.
 */
public class SparseToDenseFeatures implements DenseStateFeatures {

	protected SparseStateFeatures sparseStateFeatures;


	/**
	 * Initializes.
	 * @param sparseStateFeatures the sparse features to use
	 */
	public SparseToDenseFeatures(SparseStateFeatures sparseStateFeatures){
		this.sparseStateFeatures = sparseStateFeatures;
	}

	public SparseStateFeatures getSparseStateFeatures() {
		return sparseStateFeatures;
	}

	public void setSparseStateFeatures(SparseStateFeatures sparseStateFeatures) {
		this.sparseStateFeatures = sparseStateFeatures;
	}

	@Override
	public double[] features(State s) {

		List<StateFeature> sfs = this.sparseStateFeatures.features(s);
		double [] fv = new double[this.sparseStateFeatures.numFeatures()];
		for(StateFeature sf : sfs){
			fv[sf.id] = sf.value;
		}
		return fv;
	}

	@Override
	public SparseToDenseFeatures copy() {
		return new SparseToDenseFeatures(sparseStateFeatures);
	}
}
