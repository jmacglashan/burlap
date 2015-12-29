package burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.ConditionLearners;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;

public class PerceptionConditionLearnerPF extends PropositionalFunction {
	
	PerceptionConditionLearner percCondLearner;
	
	public PerceptionConditionLearnerPF(String name, Domain domain,
			String parameterClasses, PerceptionConditionLearner percCondLearner) {
		super(name, domain, parameterClasses);
		this.percCondLearner = percCondLearner;
		this.percCondLearner.trainClassifier();
	}


	@Override
	public boolean isTrue(State s, String[] params) {
		return this.percCondLearner.predict(s);
	}
	
}
