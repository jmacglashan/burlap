package burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.ConditionLearners;

import java.util.List;

import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.ConditionHypothesis;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.StateHelpers;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;

/**
 * Wrapper class to learn a condition hypothesis for a given condition
 * @author Dhershkowitz
 *
 */
public class PFConditionLearner extends OOMDPConditionLearner{

	List<PropositionalFunction> propFuns;
	private ConditionHypothesis HSubT;

	/**
	 * 
	 */
	public PFConditionLearner(List<PropositionalFunction> propFuns){
		this.HSubT = null;
		this.propFuns = propFuns;
	}

	/**
	 * 
	 * @return the CH that predicts true for this CL
	 */
	private ConditionHypothesis getHSubT() {
		return this.HSubT;
	}

	/**
	 * 
	 * @param otherCL
	 * @return
	 */
	private boolean conditionsOverlap(PFConditionLearner otherCL) {
		boolean toReturn = this.HSubT.matches(otherCL.getHSubT()) || otherCL.HSubT.matches(this.getHSubT());
		return toReturn;
	}

	/**
	 * 
	 * @param currStatePreds state to test the truth of the condition in
	 * @return a boolean of if the condition entails the state
	 */
	private boolean conditionTrueInState(int [] currStatePreds){
		if (this.HSubT == null) return false;
		return this.HSubT.matches(currStatePreds);
	}

	/**
	 * 
	 * @param statePreds s state as a series of truth bits for propositional functions in which the condition was true
	 */
	private void updateVersionSpace(int [] statePreds) {

		//New HSubT
		if (HSubT == null) {
			HSubT = new ConditionHypothesis(statePreds);
		}
		//HSubT already instantiated
		else {
			HSubT = HSubT.xor(statePreds);
		}
	}


	@Override
	public String toString() {
		if (this.HSubT != null) {
			return this.HSubT.toString();
		}

		return null;
	}

	@Override
	public void learn(State s, boolean trueInState) {
		int [] statePreds = StateHelpers.stateToBitStringOfPreds(s, this.propFuns);
		if (trueInState) {
			this.updateVersionSpace(statePreds);
		}
	}

	@Override
	public Boolean predict(State s) {
		int [] statePreds = StateHelpers.stateToBitStringOfPreds(s, this.propFuns);
		return this.conditionTrueInState(statePreds);
	}

	@Override
	public boolean conditionsOverlap(OOMDPConditionLearner otherLearner) {
		return this.HSubT.matches(((PFConditionLearner) otherLearner).getHSubT()) ||
				(((PFConditionLearner) otherLearner).getHSubT()).matches(this.HSubT);
	}

}
