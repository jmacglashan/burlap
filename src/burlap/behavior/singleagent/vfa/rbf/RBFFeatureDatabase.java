package burlap.behavior.singleagent.vfa.rbf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.behavior.singleagent.vfa.ActionFeaturesQuery;
import burlap.behavior.singleagent.vfa.FeatureDatabase;
import burlap.behavior.singleagent.vfa.StateFeature;
import burlap.behavior.singleagent.vfa.ValueFunctionApproximation;
import burlap.behavior.singleagent.vfa.common.LinearVFA;
import burlap.oomdp.core.AbstractObjectParameterizedGroundedAction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;


/**
 * A feature database of RBF units that can be used for linear value function approximation.
 * This formalization for RBFs is general to any kind of state distance measure and can therefore
 * potentially exploit the OO-MDP structure of states. However, if you plan on using Gaussian
 * RBF units with standard Euclidean distance measures, it is recommended that you use the
 * {@link burlap.behavior.singleagent.vfa.rbf.FVRBFFeatureDatabase} instead, as it will be
 * more computationally efficient.
 * @author Anubhav Malhotra and Daniel Fernandez and Spandan Dutta; modified by James MacGlashan
 *
 */
public class RBFFeatureDatabase implements FeatureDatabase {

	
	/**
	 * The list of RBF units in this database
	 */
	protected List<RBF> rbfs;
	
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
	protected Map<GroundedAction, Integer> actionFeatureMultiplier = new HashMap<GroundedAction, Integer>();
	
	/**
	 * The next action RBF size multiplier to use for the next newly seen action.
	 */
	protected int nextActionMultiplier = 0;
	
	
	/**
	 * Initializes with an empty list of RBF units.
	 * @param hasOffset if true, an offset RBF unit with a constant response value is included in the feature set.
	 */
	public RBFFeatureDatabase(boolean hasOffset){
		rbfs = new ArrayList<RBF>();
		this.hasOffset = hasOffset;
		
		if(hasOffset)
		{
			nRbfs = 1;
		}
	}
	
	/**
	 * Adds the specified RBF unit to the list of RBF units.
	 * @param rbf the RBF unit to add.
	 */
	public void addRBF(RBF rbf)
	{
		this.rbfs.add(rbf);
		nRbfs++;
	}

	/**
	 * Adds all of the specified RBF units to this object's list of RBF units.
	 * @param rbfs the RBF units to add.
	 */
	public void addRBFs(List<RBF> rbfs){
		this.nRbfs += rbfs.size();
		this.rbfs.addAll(rbfs);
	}

	@Override
	public List<StateFeature> getStateFeatures(State s)
	{
		
		List<StateFeature> rbfsf = new ArrayList<StateFeature>();
		int id = 0;
		
		for(RBF r : rbfs)
		{
			double value = r.responseFor(s);
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
		
		if(ga instanceof AbstractObjectParameterizedGroundedAction){
			throw new RuntimeException("RBF Feature Database does not support actions with AbstractObjectParameterizedGroundedActions.");
		}
		
		Integer stored = this.actionFeatureMultiplier.get(ga);
		if(stored == null){
			this.actionFeatureMultiplier.put(ga, this.nextActionMultiplier);
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
