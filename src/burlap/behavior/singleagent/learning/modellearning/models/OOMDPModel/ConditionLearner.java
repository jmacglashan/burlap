package burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ConditionLearner {

	public HashSet<ConditionHypothesis> HHat;

	public ConditionHypothesis HSubT;
	private int numPreds;
	private List<ConditionHypothesis> observedStatePreds;

	public ConditionLearner(int numPreds){
		this.HHat = new HashSet<ConditionHypothesis>();
		this.HSubT = null;
		this.numPreds = numPreds;
		this.initializeHypotheses();

		this.observedStatePreds = new ArrayList<ConditionHypothesis>();
	}

	public boolean conditionsOverlap(ConditionLearner otherCL) {
		boolean toReturn = this.HSubT.matches(otherCL.HSubT) || otherCL.HSubT.matches(this.HSubT);
		return toReturn;
	}


	public List<Boolean> computePredictions(int [] currStatePreds){
		List<Boolean> toReturn = new ArrayList<Boolean>();

		//Check for hyp that predicts false
		for(ConditionHypothesis h: HHat) {
			if (h.matches(currStatePreds) && !h.getTruthVal()) {
				toReturn.add(false);
				break;
			}
		}

		//Check for hyp that predicts true
		for(ConditionHypothesis h: HHat) {
			if (h.matches(currStatePreds) && h.getTruthVal()) {
				toReturn.add(true);
				break;
			}
		}


		//predict true if hsubt is null or matches
		//		if(this.HSubT == null || this.HSubT != null && this.HSubT.matches(currStatePreds)) {
		//			toReturn.add(true);
		//		}

		return toReturn;
	}

	public boolean knownForState(int [] currStatePreds){
		List<Boolean> preds = new ArrayList<Boolean>();

		//Check for hyp that predicts false
		for(ConditionHypothesis h: HHat) {
			if (h.matches(currStatePreds) && !h.getTruthVal()) {
				preds.add(false);
				break;
			}
		}

		//Check for hyp that predicts true
		for(ConditionHypothesis h: HHat) {
			if (h.matches(currStatePreds) && h.getTruthVal()) {
				preds.add(true);
				break;
			}
		}

		return (preds.size() == 1);
	}

	public void updateVersionSpace(boolean observation, int [] statePreds) {
		//Add to seen observations
		ConditionHypothesis obsHyp =new ConditionHypothesis(statePreds, observation);
		if (!this.observedStatePreds.contains(obsHyp)) this.observedStatePreds.add(obsHyp);

		//True observation
		if (observation) {
			//Update HsubT
			if (HSubT == null) {
				HSubT = obsHyp;
			}
			else {
				HSubT = HSubT.xor(statePreds);
			}
			//Eliminate hyps that predict false
			List<ConditionHypothesis> toEliminate = new ArrayList<ConditionHypothesis>();
			for (ConditionHypothesis h: HHat) {
				if(!h.getTruthVal() && HSubT.matches(h)) {
					toEliminate.add(h);
				}
			}
			for (ConditionHypothesis h: toEliminate) {
				HHat.remove(h);
			}


		}

		//False observation
		else {	
			//Eliminate the hyp that predicts true
			for (ConditionHypothesis h: HHat) {
				if (h.matches(statePreds) && h.getTruthVal()) {
					HHat.remove(h);
					break;
				}

			}	
		}
	}

	private void initializeHypotheses() {
		List<int []> statePreds = new ArrayList<int []>();
		statePreds.add(new int[this.numPreds]);

		//Get power set of 1s and 0s
		for (int i = 0; i < this.numPreds; i++) {
			List<int []> newStatePreds = new ArrayList<int []>();
			for (int j = 0; j < statePreds.size(); j++) {
				int [] currStatePred = statePreds.get(j);
				newStatePreds.add(currStatePred);
				int [] predWith1 = currStatePred.clone();
				predWith1[i] = 1;
				newStatePreds.add(predWith1);

			}

			statePreds = newStatePreds;
		}

		//Add all hypotheses
		for (int [] statePred: statePreds) {
			HHat.add(new ConditionHypothesis(statePred, true));
			HHat.add(new ConditionHypothesis(statePred, false));
		}

	}

	public String hypothesesToString() {
		StringBuilder toReturn = new StringBuilder();
		for(ConditionHypothesis h: HHat) {
			toReturn.append(h.toString());
		}
		return toReturn.toString();
	}

	@Override
	public String toString() {
		String condLearner = null;
		if (this.HSubT != null) {
			condLearner = this.HSubT.toString();
		}

		return "Known true condition is " + condLearner;
	}

	public String getObservedStatePreds() {
		StringBuilder toReturn = new StringBuilder();



		for (ConditionHypothesis obs : this.observedStatePreds) {
			toReturn.append(obs.toString());
		}


		return "(" + toReturn.toString() + ")";
	}

	public static void main(String[] args) {
		ConditionLearner test = new ConditionLearner(3);
		test.initializeHypotheses();

		test.updateVersionSpace(true, new int [] {1, 1, 1});


		test.updateVersionSpace(true, new int [] {1, 0, 1});

		test.updateVersionSpace(true, new int [] {1, 1, 1});

		for (ConditionHypothesis h : test.HHat) {
			System.out.println(h.toString());
		}

		System.out.println(test.HSubT);
		System.out.println(test.computePredictions(new int [] {1, 1, 1}));


	}



}
