package burlap.behavior.functionapproximation.dense;

import burlap.mdp.core.AbstractGroundedAction;
import burlap.mdp.core.state.State;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that generates state-action features as cross product of underlying state-features with the action set.
 * @author James MacGlashan.
 */
public class DenseCrossProductFeatures implements DenseStateActionFeatures {

	/**
	 * The state features
	 */
	protected DenseStateFeatures stateFeatures;

	/**
	 * The number of possible actions
	 */
	protected int numActions;

	/**
	 * A feature index offset for each action when using Q-value function approximation.
	 */
	protected Map<AbstractGroundedAction, Integer> actionOffset = new HashMap<AbstractGroundedAction, Integer>();


	public DenseCrossProductFeatures(DenseStateFeatures stateFeatures, int numActions) {
		this.stateFeatures = stateFeatures;
		this.numActions = numActions;
	}

	public DenseCrossProductFeatures(DenseStateFeatures stateFeatures, int numActions, Map<AbstractGroundedAction, Integer> actionOffset) {
		this.stateFeatures = stateFeatures;
		this.numActions = numActions;
		this.actionOffset = actionOffset;
	}

	public DenseStateFeatures getStateFeatures() {
		return stateFeatures;
	}

	public void setStateFeatures(DenseStateFeatures stateFeatures) {
		this.stateFeatures = stateFeatures;
	}

	public int getNumActions() {
		return numActions;
	}

	public void setNumActions(int numActions) {
		this.numActions = numActions;
	}

	@Override
	public double[] features(State s, AbstractGroundedAction a) {

		double [] sFeatures = stateFeatures.features(s);
		double [] saFeatures = new double[sFeatures.length*numActions];
		int offset = this.getActionOffset(a);
		for(int i = offset; i < sFeatures.length; i++){
			saFeatures[i] = sFeatures[i - offset];
		}

		return saFeatures;
	}

	@Override
	public DenseStateActionFeatures copy() {
		return new DenseCrossProductFeatures(stateFeatures, numActions, actionOffset);
	}



	public int getActionOffset(AbstractGroundedAction a){
		Integer offset = this.actionOffset.get(a);
		if(offset == null){
			offset = this.actionOffset.size();
			this.actionOffset.put(a, offset);
		}
		return offset;
	}

}
