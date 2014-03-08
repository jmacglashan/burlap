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
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;


/**
 * A feature database of RBF units that can be used for linear value function approximation.
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
	 * A map for return a multiplier to the number of RBF statefeatures for each action. Effecitively
	 * this ensures a unieque feature ID fo reach RBF for each action.
	 */
	protected Map<String, Integer> actionFeatureMultiplier = new HashMap<String, Integer>();
	
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
		
		if(ga.isParameterized()){
			throw new RuntimeException("RBF Feature Database does not support parameterized actions.");
		}
		
		Integer stored = this.actionFeatureMultiplier.get(ga.actionName());
		if(stored == null){
			this.actionFeatureMultiplier.put(ga.actionName(), this.nextActionMultiplier);
			stored = this.nextActionMultiplier;
			this.nextActionMultiplier++;
		}
		
		return stored;
	}

}
