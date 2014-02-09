package burlap.behavior.singleagent.vfa.rbf;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.singleagent.vfa.ActionFeaturesQuery;
import burlap.behavior.singleagent.vfa.FeatureDatabase;
import burlap.behavior.singleagent.vfa.LinearVFA;
import burlap.behavior.singleagent.vfa.StateFeature;
import burlap.behavior.singleagent.vfa.ValueFunctionApproximation;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;


/**
 * A feature database of RBF units that can be used for linear value function approximation.
 * @author James MacGlashan
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
		int actionCount = actions.size();
		int id = 0;
		
		for(GroundedAction ga : actions)
		{
			ActionFeaturesQuery afq = new ActionFeaturesQuery(ga);
			lstAFQ.add(afq);
		}
		
		for(RBF r : rbfs)
		{
			double value = r.responseFor(s);
			
			for(int i = 0; i < actionCount; i++)
			{
				StateFeature sf = new StateFeature(id, value);
				lstAFQ.get(i).addFeature(sf);
				id++;
			}
		}
		
		if(hasOffset)
		{
			for(int i = 0; i < actionCount; i++)
			{
				StateFeature sf = new StateFeature(id, 1.0);
				lstAFQ.get(i).addFeature(sf);
				id++;
			}
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

}
