package burlap.behavior.singleagent.vfa.rbf;

import burlap.behavior.singleagent.vfa.*;
import burlap.behavior.singleagent.vfa.common.LinearVFA;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * A feature database of RBF units that can be used for linear value function approximation.
 * Unlike, {@link burlap.behavior.singleagent.vfa.rbf.RBFFeatureDatabase}, this version takes as input
 * a {@link burlap.behavior.singleagent.vfa.StateToFeatureVectorGenerator} object so that states are first converted
 * to a double array and then provided to {@link burlap.behavior.singleagent.vfa.rbf.FVRBF} objects that operate
 * directly on the feature vector of the state. This can improve performance over the
 * {@link burlap.behavior.singleagent.vfa.rbf.RBFFeatureDatabase} which may require generating a feature vector
 * for states for each RBF unit. Therefore, for RBFs like ones that  use Gaussian functions over
 * Euclidean distance metrics, this class is recommended. However, the standard {@link burlap.behavior.singleagent.vfa.rbf.RBFFeatureDatabase}
 * may have the advantage of using RBF units that exploit the OO-MDP state representation for distance measures.
 *
 * @author James MacGlashan.
 */
public class FVRBFFeatureDatabase implements FeatureDatabase{

	/**
	 * The state feature vector generator to supply to the RBFs
	 */
	protected StateToFeatureVectorGenerator fvGen;

	/**
	 * The list of RBF units in this database
	 */
	protected List<FVRBF> rbfs;

	/**
	 * The number of RBF units, not including an offset unit.
	 */
	protected int nRbfs;

	/**
	 * Specifies whether an offset RBF unit with a constant response value is included in the feature set.
	 */
	protected boolean hasOffset;

	/**
	 * A map for returning a multiplier to the number of RBF state features for each action. Effectively
	 * this ensures a unique feature ID for each RBF for each action.
	 */
	protected Map<String, Integer> actionFeatureMultiplier = new HashMap<String, Integer>();

	/**
	 * The next action RBF size multiplier to use for the next newly seen action.
	 */
	protected int nextActionMultiplier = 0;


	/**
	 * Initializes with an empty list of RBF units.
	 * @param fvGen the state feature vector generator to use to generate the feature vectors provided to RBFs
	 * @param hasOffset if true, an offset RBF unit with a constant response value is included in the feature set.
	 */
	public FVRBFFeatureDatabase(StateToFeatureVectorGenerator fvGen, boolean hasOffset){
		rbfs = new ArrayList<FVRBF>();
		this.hasOffset = hasOffset;
		this.fvGen = fvGen;

		if(hasOffset)
		{
			nRbfs = 1;
		}
	}

	/**
	 * Adds the specified RBF unit to the list of RBF units.
	 * @param rbf the RBF unit to add.
	 */
	public void addRBF(FVRBF rbf)
	{
		this.rbfs.add(rbf);
		nRbfs++;
	}

	/**
	 * Adds all of the specified RBF units to this object's list of RBF units.
	 * @param rbfs the RBF units to add.
	 */
	public void addRBFs(List<FVRBF> rbfs){
		this.nRbfs += rbfs.size();
		this.rbfs.addAll(rbfs);
	}

	@Override
	public List<StateFeature> getStateFeatures(State s)
	{

		List<StateFeature> rbfsf = new ArrayList<StateFeature>();
		int id = 0;

		double [] svars = this.fvGen.generateFeatureVectorFrom(s);

		for(FVRBF r : rbfs)
		{
			double value = r.responseFor(svars);
			StateFeature sf = new StateFeature(id, value);
			rbfsf.add(sf);
			id++;
		}

		if(hasOffset)
		{
			StateFeature sf = new StateFeature(id, 1);
			rbfsf.add(sf);
		}

		return rbfsf;
	}

	@Override
	public List<ActionFeaturesQuery> getActionFeaturesSets(State s, List<GroundedAction> actions)
	{

		List<ActionFeaturesQuery> lstAFQ = new ArrayList<ActionFeaturesQuery>();

		List<StateFeature> sfs = this.getStateFeatures(s);

		for(GroundedAction ga : actions){
			int actionMult = this.getActionMultiplier(ga);
			int indexOffset = actionMult*this.nRbfs;

			ActionFeaturesQuery afq = new ActionFeaturesQuery(ga);
			for(StateFeature sf : sfs){
				afq.addFeature(new StateFeature(sf.id + indexOffset, sf.value));
			}

			lstAFQ.add(afq);

		}

		return lstAFQ;
	}

	@Override
	public void freezeDatabaseState(boolean toggle)
	{
		//do nothing
	}


	/**
	 * Creates and returns a linear VFA object over this RBF feature database.
	 * @param defaultWeightValue the default feature weight value to use for all features
	 * @return a linear VFA object over this RBF feature database.
	 */
	public ValueFunctionApproximation generateVFA(double defaultWeightValue)
	{
		return new LinearVFA(this, defaultWeightValue);
	}



	/**
	 * This method returns the action multiplier for the specified grounded action.
	 * If the action is not stored, a new action multiplier will created, stored, and returned.
	 * If the action is parameterized a runtime exception is thrown.
	 * @param ga the grounded action for which the multiplier will be returned
	 * @return the action multiplier to be applied to a state feature id.
	 */
	protected int getActionMultiplier(GroundedAction ga){

		if(ga.isParameterized() && ga.action.parametersAreObjects()){
			throw new RuntimeException("RBF Feature Database does not support actions with OO-MDP object parameterizations.");
		}

		Integer stored = this.actionFeatureMultiplier.get(ga.toString());
		if(stored == null){
			this.actionFeatureMultiplier.put(ga.toString(), this.nextActionMultiplier);
			stored = this.nextActionMultiplier;
			this.nextActionMultiplier++;
		}

		return stored;
	}

	@Override
	public int numberOfFeatures() {
		if(this.actionFeatureMultiplier.size() == 0){
			return this.nRbfs;
		}
		return this.nRbfs*this.nextActionMultiplier;
	}

}
