package burlap.behavior.singleagent.learning.modellearning.models.PerceptualModel;

import java.util.List;

import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;

public class PerceptionConditionLearner {

	GroupOfPerceptions observedPerceptions;
	boolean classifierTrained = false;

	public PerceptionConditionLearner() {
		this.classifierTrained = false;
	}

	public void learn(State s, GroundedAction ga, State sPrime, boolean effectOcurred) {
		StatePerception sPerc = new FullAttributeStatePerception(s, true);
		observedPerceptions.addPerception(sPerc);
		this.classifierTrained = false;
	}

	public boolean predict(State s) {
		//Train classifier if not trained 
		if (!classifierTrained) {
			trainClassifier();
			this.classifierTrained = true;
		}

		StatePerception toClassify = new FullAttributeStatePerception(s, null);
		return this.classify(toClassify);

	}

	private void trainClassifier() {
		//TODO: train
	}

	/**
	 * Assumes trained classifier
	 * @param toClassify
	 * @return
	 */
	private boolean classify(StatePerception toClassify) {
		//TODO: classify with WEKA
		return false;
	}

}



