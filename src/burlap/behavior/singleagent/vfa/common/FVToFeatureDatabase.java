package burlap.behavior.singleagent.vfa.common;

import burlap.behavior.singleagent.vfa.ActionFeaturesQuery;
import burlap.behavior.singleagent.vfa.FeatureDatabase;
import burlap.behavior.singleagent.vfa.StateFeature;
import burlap.behavior.singleagent.vfa.StateToFeatureVectorGenerator;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class wrapper for converting a {@link burlap.behavior.singleagent.vfa.StateToFeatureVectorGenerator} into a
 * {@link burlap.behavior.singleagent.vfa.FeatureDatabase}. One advantage of this approach is that this can be used
 * to automatically construct state-action features. Specifically, the state features produced but the
 * {@link burlap.behavior.singleagent.vfa.StateToFeatureVectorGenerator} are duplicated into unique features for
 * each possible action. The duplication of features for actions is lazy. That is, every time a previously unseen
 * action is queried in the {@link #getActionFeaturesSets(burlap.oomdp.core.states.State, java.util.List)} method,
 * it gets new state features assigned to it.
 * @author James MacGlashan.
 */
public class FVToFeatureDatabase implements FeatureDatabase {

	/**
	 * The state feature vector generator
	 */
	protected StateToFeatureVectorGenerator fvGen;

	/**
	 * The dimensionality of the state features produced by the feature vector generator
	 */
	protected int dim;

	/**
	 * A map for returning a multiplier to the number of state features for each action. Effectively
	 * this ensures a unique feature ID for each state feature for each action.
	 */
	protected Map<GroundedAction, Integer> actionFeatureMultiplier = new HashMap<GroundedAction, Integer>();

	/**
	 * The next action Fourier basis function size multiplier to use for the next newly seen action.
	 */
	protected int nextActionMultiplier = 0;



	/**
	 * Initializes.
	 * @param fvGen The state feature vector generator
	 * @param dim The dimensionality of the state features produced by the feature vector generator
	 */
	public FVToFeatureDatabase(StateToFeatureVectorGenerator fvGen, int dim){
		this.fvGen = fvGen;
		this.dim = dim;
	}




	@Override
	public List<StateFeature> getStateFeatures(State s) {

		double [] vec = this.fvGen.generateFeatureVectorFrom(s);
		List<StateFeature> sfs = new ArrayList<StateFeature>(vec.length);
		for(int i = 0; i < vec.length; i++){
			if(vec[i] != 0.) {
				sfs.add(new StateFeature(i, vec[i]));
			}
		}

		return sfs;
	}

	@Override
	public List<ActionFeaturesQuery> getActionFeaturesSets(State s, List<GroundedAction> actions) {

		List<ActionFeaturesQuery> afqs = new ArrayList<ActionFeaturesQuery>(actions.size());
		double [] vec = fvGen.generateFeatureVectorFrom(s);
		for(GroundedAction ga : actions){
			List<StateFeature> sfs = new ArrayList<StateFeature>(vec.length);
			int offset = this.getActionMultiplier(ga)*dim;
			for(int i = 0; i < vec.length; i++){
				if(vec[i] != 0.) {
					sfs.add(new StateFeature(offset + i, vec[i]));
				}
			}
			ActionFeaturesQuery afq = new ActionFeaturesQuery(ga, sfs);
			afqs.add(afq);
		}

		return afqs;
	}

	@Override
	public void freezeDatabaseState(boolean toggle) {
		//do nothing
	}

	@Override
	public int numberOfFeatures() {
		if(this.nextActionMultiplier == 0){
			return dim;
		}
		return dim*this.nextActionMultiplier;
	}



	/**
	 * This method returns the action multiplier for the specified grounded action.
	 * If the action is not stored, a new action multiplier will created, stored, and returned.
	 * @param ga the grounded action for which the multiplier will be returned
	 * @return the action multiplier to be applied to a state feature id.
	 */
	protected int getActionMultiplier(GroundedAction ga){

		Integer stored = this.actionFeatureMultiplier.get(ga);
		if(stored == null){
			this.actionFeatureMultiplier.put(ga, this.nextActionMultiplier);
			stored = this.nextActionMultiplier;
			this.nextActionMultiplier++;
		}

		return stored;
	}

	@Override
	public FVToFeatureDatabase copy() {
		FVToFeatureDatabase fvfd = new FVToFeatureDatabase(this.fvGen, this.dim);
		fvfd.actionFeatureMultiplier = new HashMap<GroundedAction, Integer>(this.actionFeatureMultiplier);
		fvfd.nextActionMultiplier = this.nextActionMultiplier;
		return fvfd;
	}
}
