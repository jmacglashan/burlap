package burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel;

import java.util.List;

import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;


/**
 * Wrapper class to store a hypothesis for a condition in a state
 * @author Dhershkowitz
 *
 */
public class ConditionHypothesis {
	
	private int [] condition;
	
	/**
	 * 
	 * @param s initial state that condition was observed in
	 * @param propFuns propositional functions thought to bear on the state
	 */
	public ConditionHypothesis(State s, List<PropositionalFunction> propFuns){
		int [] statePreds = StateHelpers.stateToBitStringOfPreds(s, propFuns);
		this.condition = statePreds;
	}

	/**
	 * 
	 * @param precondition the initial state as an int [] of 0s and 1s of propositional functions
	 */
	public ConditionHypothesis(int [] precondition){
		this.condition = precondition;
	}
	
	/**
	 * 
	 * @return the related int [] for this condition hypothesis
	 */
	public int[] getCondition() {
		return this.condition;
	}

	/**
	 * 
	 * @param otherHyp the hypothesis against which to compare
	 * @return true if corresponding indices are the same or a * in this hypothesis (note that this is not a symmetric function)
	 */
	public Boolean matches(ConditionHypothesis otherHyp) {
		for (int i = 0; i < this.condition.length; i++) {
			int currVal = this.condition[i];
			int currOtherVal = otherHyp.getCondition()[i];
			if (currVal != -1 && currVal != currOtherVal) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param otherHyp the hypothesis against which to compare
	 * @return true if corresponding indices are the same or a * in this hypothesis (note that this is not a symmetric function)
	 */
	public Boolean matches(int [] otherHyp) {
		for (int i = 0; i < this.condition.length; i++) {
			int currVal = this.condition[i];
			int currOtherVal = otherHyp[i];
			if (currVal != -1 && currVal != currOtherVal) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param otherHyp the hypothesis with which to perform the bitwise xor as defined in the paper
	 * @return a new CH that is * where the hypothesis differ and whatever either was where they are the same
	 */
	public ConditionHypothesis xor(ConditionHypothesis otherHyp) {
		int [] toReturn = new int [condition.length];

		for (int i = 0; i < condition.length; i++) {
			int currVal = this.condition[i];
			int currOtherVal = otherHyp.getCondition()[i];

			if (currVal == 0 && currOtherVal == 0)
				toReturn[i] = 0;
			else if (currVal == 1 && currOtherVal == 1)
				toReturn[i] = 1;
			else toReturn[i] = -1;
		}			

		return new ConditionHypothesis(toReturn);
	}

	/**
	 * 
	 * @param otherHyp the hypothesis with which to perform the bitwise xor as defined in the paper
	 * @return a new CH that is * where the hypothesis differ and whatever either was where they are the same
	 */
	public ConditionHypothesis xor(int [] otherHyp) {
		int [] toReturn = new int [condition.length];

		for (int i = 0; i < condition.length; i++) {
			int currVal = this.condition[i];
			int currOtherVal = otherHyp[i];

			if (currVal == 0 && currOtherVal == 0)
				toReturn[i] = 0;
			else if (currVal == 1 && currOtherVal == 1)
				toReturn[i] = 1;
			else toReturn[i] = -1;
		}			


		return new ConditionHypothesis(toReturn);
	}

	@Override
	public String toString() {
		StringBuilder toReturn = new StringBuilder();

		for (int i = 0; i < this.condition.length; i++) {
			if (this.condition[i] == -1) {
				toReturn.append("*");
			}
			else {
				toReturn.append(this.condition[i]);				
			}


		}

		return new String("(" + toReturn + ")");

	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ConditionHypothesis) {
			
			if (this.condition.length != ((ConditionHypothesis) o).condition.length) return false;

			int index = 0;
			for (int curr : this.condition) {
				if (curr != ((ConditionHypothesis) o).condition[index]) return false;

				index += 1;
			}

			return true;
		}

		return false;
	}
}
