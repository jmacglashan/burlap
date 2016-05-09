package burlap.behavior.functionapproximation.sparse;

import burlap.mdp.core.AbstractGroundedAction;
import burlap.mdp.core.state.State;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link SparseStateActionFeatures} implementation that takes as input a {@link SparseStateFeatures} object,
 * and turns it into state-action features taking the cross product of the features with the action set. This
 * implementation is lazy and creates state-action features as they are queried. Consequently, the state-action feature indices
 * for an action may not be consecutive.
 * @author James MacGlashan.
 */
public class SparseCrossProductFeatures implements SparseStateActionFeatures{

	protected SparseStateFeatures sFeatures;
	protected Map<AbstractGroundedAction, FeaturesMap> actionFeatures = new HashMap<AbstractGroundedAction, FeaturesMap>();
	protected int nextFeatureId = 0;

	public SparseCrossProductFeatures(SparseStateFeatures sFeatures) {
		this.sFeatures = sFeatures;
	}

	protected SparseCrossProductFeatures(SparseStateFeatures sFeatures, Map<AbstractGroundedAction, FeaturesMap> actionFeatures, int nextFeatureId) {
		this.sFeatures = sFeatures;
		this.actionFeatures = actionFeatures;
		this.nextFeatureId = nextFeatureId;
	}

	@Override
	public List<StateFeature> features(State s, AbstractGroundedAction a) {
		List<StateFeature> sfs = sFeatures.features(s);
		List<StateFeature> safs = new ArrayList<StateFeature>(sfs.size());
		for(StateFeature sf : sfs){
			StateFeature saf = new StateFeature(actionFeature(a, sf.id), sf.value);
			safs.add(saf);
		}
		return safs;
	}

	@Override
	public SparseCrossProductFeatures copy() {
		Map<AbstractGroundedAction, FeaturesMap> nfeatures = new HashMap<AbstractGroundedAction, FeaturesMap>(actionFeatures.size());
		for(Map.Entry<AbstractGroundedAction, FeaturesMap> e : actionFeatures.entrySet()){
			nfeatures.put(e.getKey(), e.getValue().copy());
		}
		return new SparseCrossProductFeatures(sFeatures.copy(), nfeatures, nextFeatureId);
	}

	@Override
	public int numFeatures() {
		return this.sFeatures.numFeatures()*nextFeatureId;
	}

	protected int actionFeature(AbstractGroundedAction a, int from){
		FeaturesMap fmap = this.actionFeatures.get(a);
		if(a == null){
			fmap = new FeaturesMap();
			this.actionFeatures.put(a, fmap);
		}
		return fmap.getOrCreate(from);
	}




	protected class FeaturesMap{
		Map<Integer, Integer> featuresMap = new HashMap<Integer, Integer>();

		public FeaturesMap() {
		}

		public FeaturesMap(Map<Integer, Integer> featuresMap) {
			this.featuresMap = featuresMap;
		}

		public void put(int from, int to){
			this.featuresMap.put(from, to);
		}

		public int getOrCreate(int from){
			Integer to = this.featuresMap.get(from);
			if(to == null){
				to = nextFeatureId++;
				this.featuresMap.put(from, to);
			}
			return to;
		}

		public FeaturesMap copy(){
			return new FeaturesMap(new HashMap<Integer, Integer>(featuresMap));
		}

	}
}
