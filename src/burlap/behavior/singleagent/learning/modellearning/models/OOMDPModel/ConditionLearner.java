package burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel;

/**
 * Wrapper class to learn a condition hypothesis for a given condition
 * @author Dhershkowitz
 *
 */
public class ConditionLearner {

	private ConditionHypothesis HSubT;

	/**
	 * 
	 */
	public ConditionLearner(){
		this.HSubT = null;
	}

	/**
	 * 
	 * @return the CH that predicts true for this CL
	 */
	public ConditionHypothesis getHSubT() {
		return this.HSubT;
	}

	/**
	 * 
	 * @param otherCL
	 * @return
	 */
	public boolean conditionsOverlap(ConditionLearner otherCL) {
		boolean toReturn = this.HSubT.matches(otherCL.getHSubT()) || otherCL.HSubT.matches(this.getHSubT());
		return toReturn;
	}

	/**
	 * 
	 * @param currStatePreds state to test the truth of the condition in
	 * @return a boolean of if the condition entails the state
	 */
	public boolean conditionTrueInState(int [] currStatePreds){
		if (this.HSubT == null) return false;
		return this.HSubT.matches(currStatePreds);
	}

	/**
	 * 
	 * @param statePreds s state as a series of truth bits for propositional functions in which the condition was true
	 */
	public void updateVersionSpace(int [] statePreds) {

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
		String condLearner = null;
		if (this.HSubT != null) {
			condLearner = this.HSubT.toString();
		}

		return "CLearner: known true condition is " + condLearner;
	}

}
