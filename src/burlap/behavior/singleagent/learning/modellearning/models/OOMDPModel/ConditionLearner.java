package burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ConditionLearner {
	
	public HashSet<Hypothesis> HHat;
	
	private Hypothesis HSubT;
	private int numPreds;
	
	public ConditionLearner(int numPreds){
		this.HHat = new HashSet<Hypothesis>();
		this.HSubT = null;
		this.numPreds = numPreds;
	}
	
	
	public List<Boolean> computePredictions(int [] currStatePreds){
		List<Boolean> toReturn = new ArrayList<Boolean>();
		
		//Check for hyp that predicts false
		for(Hypothesis h: HHat) {
			if (h.matches(currStatePreds) && !h.getTruthVal()) {
				toReturn.add(false);
				break;
			}
		}
		
		//Check for hyp that predicts true
		if (this.HSubT == null || (this.HSubT.matches(currStatePreds))) {
			toReturn.add(true);
		}
		
		return toReturn;
	}
	
	public void updateVersionSpace(boolean observation, int [] statePreds) {
		//True observation
		if (observation) {
			//Update prediction
			if (HSubT == null) {
				HSubT = new Hypothesis(statePreds, true);
			}
			else {
				HSubT = HSubT.xor(statePreds);
			}
			//Eliminate hyps that predict false
			List<Hypothesis> toEliminate = new ArrayList<Hypothesis>();
			for (Hypothesis h: HHat) {
				if(!h.getTruthVal() && HSubT.matches(h)) {
					toEliminate.add(h);
				}
			}
			for (Hypothesis h: toEliminate) {
				HHat.remove(h);
			}
			
			
		}
		
		//False observation
		else {	
			//Eliminate the hyp that predicts true
			for (Hypothesis h: HHat) {
				if (h.matches(statePreds) && h.getTruthVal()) {
					HHat.remove(h);
					break;
				}
				
			}	
		}
	}
	
	public void initializeHypotheses() {
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
			HHat.add(new Hypothesis(statePred, true));
			HHat.add(new Hypothesis(statePred, false));
		}
		
	}
	
	public static void main(String[] args) {
		ConditionLearner test = new ConditionLearner(3);
		test.initializeHypotheses();
		

		
		test.updateVersionSpace(true, new int [] {1, 1, 1});
		
		
		test.updateVersionSpace(true, new int [] {1, 0, 1});
		
		test.updateVersionSpace(false, new int [] {1, 1, 0});
		
		for (Hypothesis h : test.HHat) {
			System.out.println(h.toString());
		}
		
		System.out.println(test.HSubT);
		System.out.println(test.computePredictions(new int [] {1, 1, 0}));
		

	}
	
}
