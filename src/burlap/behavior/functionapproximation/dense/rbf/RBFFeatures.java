package burlap.behavior.functionapproximation.dense.rbf;

import burlap.behavior.functionapproximation.dense.DenseLinearVFA;
import burlap.behavior.functionapproximation.dense.DenseStateFeatures;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.GroundedAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * A feature database of RBF units that can be used for linear value function approximation.
 * This class takes as input
 * a {@link DenseStateFeatures} object so that states are first converted
 * to a double array and then provided to {@link RBF} objects that operate
 * directly on the feature vector of the state.
 *
 * @author James MacGlashan.
 */
public class RBFFeatures implements DenseStateFeatures {

	/**
	 * The input features over which RBFs will be generated
	 */
	protected DenseStateFeatures inputFeatures;

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
	 * @param inputFeatures the input features over which RBFs will be generated
	 * @param hasOffset if true, an offset RBF unit with a constant response value is included in the feature set.
	 */
	public RBFFeatures(DenseStateFeatures inputFeatures, boolean hasOffset){
		rbfs = new ArrayList<RBF>();
		this.hasOffset = hasOffset;
		this.inputFeatures = inputFeatures;

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
	public double [] features(State s)
	{

		int n = this.rbfs.size();
		n = hasOffset ? n+1 : n;

		double [] rbfFeatures = new double[n];



		double [] svars = this.inputFeatures.features(s);

		int id = 0;
		for(RBF r : rbfs)
		{
			double value = r.responseFor(svars);
			rbfFeatures[id] = value;
			id++;
		}

		if(hasOffset)
		{
			rbfFeatures[id] = 1.;
		}

		return rbfFeatures;
	}


	/**
	 * Creates and returns a linear VFA object over this RBF feature database.
	 * @param defaultWeightValue the default feature weight value to use for all features
	 * @return a linear VFA object over this RBF feature database.
	 */
	public DenseLinearVFA generateVFA(double defaultWeightValue)
	{
		return new DenseLinearVFA(this, defaultWeightValue);
	}




	@Override
	public RBFFeatures copy() {

		RBFFeatures rbf = new RBFFeatures(this.inputFeatures, this.hasOffset);
		rbf.rbfs = new ArrayList<RBF>(this.rbfs);
		rbf.nRbfs = this.nRbfs;
		rbf.actionFeatureMultiplier = new HashMap<GroundedAction, Integer>(this.actionFeatureMultiplier);
		rbf.nextActionMultiplier = this.nextActionMultiplier;

		return rbf;
	}

}
