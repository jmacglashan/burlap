package burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.ConditionLearners;

import burlap.oomdp.core.State;

public abstract class OOMDPConditionLearner {
	public abstract void learn(State s, boolean trueInState);
	
	public abstract Boolean predict(State s);
	public abstract boolean conditionsOverlap(OOMDPConditionLearner otherLearner);
}
