package burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel;


public class ConditionHypothesis {

		private int [] precondition;
		private boolean truthVal;


		public int[] getPrecondition() {
			return this.precondition;
		}
		
		public boolean getTruthVal() {
			return this.truthVal;
		}



		public ConditionHypothesis(int [] precondition, boolean truthVal){
			this.precondition = precondition;
			this.truthVal = truthVal;
		}

		public Boolean matches(ConditionHypothesis otherHyp) {
			for (int i = 0; i < this.precondition.length; i++) {
				int currVal = this.precondition[i];
				int currOtherVal = otherHyp.getPrecondition()[i];
				if (currVal != -1 && currVal != currOtherVal) {
					return false;
				}
			}
			return true;
		}
		
		public Boolean matches(int [] otherHyp) {
			for (int i = 0; i < this.precondition.length; i++) {
				int currVal = this.precondition[i];
				int currOtherVal = otherHyp[i];
				if (currVal != -1 && currVal != currOtherVal) {
					return false;
				}
			}
			return true;
		}

		public ConditionHypothesis xor(ConditionHypothesis otherHyp) {
			int [] toReturn = new int [precondition.length];

			for (int i = 0; i < precondition.length; i++) {
				int currVal = this.precondition[i];
				int currOtherVal = otherHyp.getPrecondition()[i];

				if (currVal == 0 && currOtherVal == 0)
					toReturn[i] = 0;
				else if (currVal == 1 && currOtherVal == 1)
					toReturn[i] = 1;
				else toReturn[i] = -1;
			}			


			return new ConditionHypothesis(toReturn, this.truthVal);
		}
		
		public ConditionHypothesis xor(int [] otherHyp) {
			int [] toReturn = new int [precondition.length];

			for (int i = 0; i < precondition.length; i++) {
				int currVal = this.precondition[i];
				int currOtherVal = otherHyp[i];

				if (currVal == 0 && currOtherVal == 0)
					toReturn[i] = 0;
				else if (currVal == 1 && currOtherVal == 1)
					toReturn[i] = 1;
				else toReturn[i] = -1;
			}			


			return new ConditionHypothesis(toReturn, this.truthVal);
		}
		
		@Override
		public String toString() {
			StringBuilder toReturn = new StringBuilder();
			
			for (int i = 0; i < this.precondition.length; i++) {
				if (this.precondition[i] == -1) {
					toReturn.append("*");
				}
				else {
					toReturn.append(this.precondition[i]);				
				}
				
				
			}
			toReturn.append(", " + this.truthVal);
			
			return new String(toReturn);
			
		}
}
