package burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects.Effect;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.core.Attribute;

public class MultipleConditionEffectsLearner {

	private HashMap<Action, List<ConditionEffectLearner>> CELearnersByAction;
	
	public MultipleConditionEffectsLearner(Domain d) {
		this.CELearnersByAction = new HashMap<Action, List<ConditionEffectLearner>>();
		List<Action> actions = d.getActions();
		List<ObjectClass> oClasses = d.getObjectClasses();
		//Instantiate CELearners for all actions attribute-object class tuples

		for (Action a: actions) {
			List<ConditionEffectLearner> listOfLearnersForAction = new ArrayList<ConditionEffectLearner>();
			for (ObjectClass oClass : oClasses) {
				for(Attribute att : oClass.attributeList) {
					ConditionEffectLearner toAdd = new ConditionEffectLearner(d, oClass, att, a);
					listOfLearnersForAction.add(toAdd);
				}
			}
			this.CELearnersByAction.put(a, listOfLearnersForAction);
		}	
	}
	
	public void learn(State s, Action a, State sPrime) {
		List<ConditionEffectLearner> toUpdate = this.CELearnersByAction.get(a);
		for (ConditionEffectLearner CELearner: toUpdate) {
			CELearner.updateLearners(s, sPrime);
		}
		//TO DO: ADD K DELETION AND OVERLAPPING DELETION CONDITIONS
	}
	
	
	public State predict(State s, Action a) {
		List<ConditionEffectLearner> relevantLearners = this.CELearnersByAction.get(a);
		State toReturn = s.copy();
		
		List<Effect> hypothesizedEffects = new ArrayList<Effect>();
		
		for (ConditionEffectLearner CELearner: relevantLearners) {
			hypothesizedEffects.add(CELearner.predictResultingEffect(s));
		}
		//If don't know
		if (hypothesizedEffects.contains(null)) return null;
		
		//TODO: CHECK FOR CONTRADICTIONS
		
		//Do know effect and no contradictions
		for (Effect e : hypothesizedEffects) {
			toReturn = e.applyEffect(toReturn);
		}

		return toReturn;
	}
	
	public String stateOfEffectsOnState(State state, Action action) {
		StringBuilder toReturn = new StringBuilder();
		
		List<ConditionEffectLearner> relevantLearners = this.CELearnersByAction.get(action);
		
		List<ConditionEffectLearner> learnersThatKnow = new ArrayList<ConditionEffectLearner>();
		List<ConditionEffectLearner> learnersThatDontKnow = new ArrayList<ConditionEffectLearner>();
		
		for (ConditionEffectLearner learner : relevantLearners) {
			if (learner.predictResultingState(state) != null) {
				learnersThatKnow.add(learner);
			}
			else {
				learnersThatDontKnow.add(learner);
			}
		}
		
		toReturn.append("State as bit string is: " + StateHelpers.stateToBitStringOfPredsString(state, relevantLearners.get(0).propFuns) + "\n");
		
		toReturn.append("CELs that know:\n");
		for (ConditionEffectLearner learner : learnersThatKnow) {
			toReturn.append(learner.knowledgeOnState(state) + "\n");
		}
		
		toReturn.append("CELs that don't know:\n");
		for (ConditionEffectLearner learner : learnersThatDontKnow) {
			toReturn.append(learner.knowledgeOnState(state) + "\n");
		}
		return toReturn.toString();
		
	}
	
	
}
