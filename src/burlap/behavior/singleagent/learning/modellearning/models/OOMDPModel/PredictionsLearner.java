package burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects.Effect;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects.EffectHelpers;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects.EffectNotFoundException;
import burlap.behavior.singleagent.learning.modellearning.rmax.TaxiDomain;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.core.Attribute;

/**
 * Class to learn all predictions for as documented in 
 * "An Object-Oriented Representation for Efficient Reinforcement Learning"
 * by Diuk et al.
 * @author Dhershkowitz
 *
 */
public class PredictionsLearner {

	private HashMap<PaperAttributeActionTuple, List<ConditionHypothesis>> failureConditionsByActionOClassAtt;
	private HashMap<AttActionEffectTuple, List<Prediction>> predictionsByAttActionAndEffect;
	private List<String> effectsToUse;
	private Domain d;
	private int k;
	private List<PropositionalFunction> propFunsToUse;
	private String statePerceptionToUse;
	private List<GroundedAction> allGAs;

	/**
	 * 
	 * @param d domain to use
	 * @param propFuns relevant prop funs to plan over (using the groundings of in initial state)
	 * @param effectsToUse effect types by string to consider (documented as static elements of EffectHelpers)
	 * @param actionsToUse relevant actions to plan over (using the groundings in initial state)
	 * @param initialState the state to groundings from
	 * @param k the max number of effects (of one type) that an action can have
	 */
	public PredictionsLearner(Domain d, List<PropositionalFunction> propFuns, List<String> effectsToUse, List<Action> actionsToUse, State initialState, int k, String statePerceptionToUse) {
		this.statePerceptionToUse = statePerceptionToUse;
		this.d = d;
		this.k = k;
		this.propFunsToUse = propFuns;
		this.allGAs = new ArrayList<GroundedAction>();
		for (Action a : actionsToUse) {
			this.allGAs.addAll(a.getAllApplicableGroundedActions(initialState));
		}
		this.effectsToUse = effectsToUse;

		//Set up HM for failure conditions
		this.failureConditionsByActionOClassAtt = new HashMap<PaperAttributeActionTuple, List<ConditionHypothesis>>();
		for (GroundedAction ga : this.allGAs) {
			for (ObjectClass oClass : this.d.getObjectClasses()) {
				for (Attribute att : oClass.attributeList) {
					PaperAttributeActionTuple toHashBy = new PaperAttributeActionTuple(oClass, att, ga);
					this.failureConditionsByActionOClassAtt.put(toHashBy, new ArrayList<ConditionHypothesis>());
				}

			}

		}

		//Set up HM for predictions
		this.predictionsByAttActionAndEffect = new HashMap<AttActionEffectTuple, List<Prediction>>();
		for (GroundedAction a : this.allGAs) {
			for (ObjectClass oClass : this.d.getObjectClasses()) {
				for (Attribute att : oClass.attributeList) {
					for (String effectType : this.effectsToUse) {
						AttActionEffectTuple toHashBy = new AttActionEffectTuple(oClass, att, a, effectType);
						this.predictionsByAttActionAndEffect.put(toHashBy, new ArrayList<Prediction>());
					}
				}	
			}
		}
	}

	public HashMap<AttActionEffectTuple, List<Prediction>> getPredictionsByAttActionAndEffect() {
		return this.predictionsByAttActionAndEffect;
	}
	
	/**
	 * 
	 * @param pred to check for overlaps with
	 * @param relevantPredictions a list of predicates to check if pred overlaps against
	 * @return a boolean of if pred's conditon overlaps with a condition in relevantLearners (other than itself)
	 */
	private boolean predictionsOverlap(Prediction pred, List<Prediction> relevantPredictions) {
		//Filter out those that don't act on same object type

		for (Prediction otherPred : relevantPredictions) {
			if (relevantPredictions.indexOf(pred) != relevantPredictions.indexOf(otherPred) && pred.overlapWithPrediction(otherPred)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param relevantPredictions a list of the predictions that might predict observedEffect
	 * @param observedEffect an effect to search for a prediction that predicts it
	 * @return the prediction that predicts observedEffect or null if none did
	 */
	private Prediction predThatPredictsThisEffect(List<Prediction> relevantPredictions, Effect observedEffect) {
		for (Prediction CELearner : relevantPredictions) {
			if (CELearner.getEffectLearningFor().equals(observedEffect)) {
				return CELearner;
			}
		}
		return null;
	}

	public void learn(State s, GroundedAction ga, State sPrime) {
		for (ObjectClass oClass : this.d.getObjectClasses()) {
			for (Attribute att : oClass.attributeList) {
				learnForOClassAndAtt(s, ga, sPrime, oClass, att);
			}
		}
	}

	private boolean classAndAttUnchanged(State s, State sPrime, ObjectClass oClass, Attribute att) {
		List<ObjectInstance> objectInstances = s.getObjectsOfTrueClass(oClass.name);
		for (ObjectInstance o : objectInstances) {
			double valBefore = o.getNumericValForAttribute(att.name);
			double valAfter = sPrime.getObject(o.getName()).getNumericValForAttribute(att.name);
			if (valBefore != valAfter) return false;
		}

		return true;
	}

	private void learnForOClassAndAtt(State s, GroundedAction a, State sPrime, ObjectClass oClass, Attribute att) {
		List<Prediction> predictionsThatPredictEffectsForState = new ArrayList<Prediction>();

		//Found a failure condition for an action -- update as necessary
		if (classAndAttUnchanged(s, sPrime, oClass, att)) {
			ConditionHypothesis failureHyp = new ConditionHypothesis(s, this.propFunsToUse);

			PaperAttributeActionTuple toHashBy = new PaperAttributeActionTuple(oClass, att, a);
			List<ConditionHypothesis> failureConditions = this.failureConditionsByActionOClassAtt.get(toHashBy);
			//Remove all conditions that are entailed by this states conditions
			List<ConditionHypothesis> hypsToRemove = new ArrayList<ConditionHypothesis>();
			for (ConditionHypothesis hyp : failureConditions) {
				if (failureHyp.matches(hyp)) {
					hypsToRemove.add(hyp);
				}
			}
			for (ConditionHypothesis hyp : hypsToRemove) {
				failureConditions.remove(hyp);
			}

			//Add this condition to the set of failure conditions
			failureConditions.add(failureHyp);
		}
		//Wasn't a failure condition -- update learners
		else {
			//Get possible effects for this oClass and attribute
			List<Effect> possibleEffects = null;
			try {
				possibleEffects = EffectHelpers.getPossibleEffects(s, sPrime, oClass, att, this.effectsToUse);
			} catch (EffectNotFoundException e) {
				e.printStackTrace();
			}

			for (Effect observedEffect: possibleEffects) {
				AttActionEffectTuple toHashBy = new AttActionEffectTuple(oClass, att, a, observedEffect.getEffectTypeString());
				List<Prediction> relevantPredictions = this.predictionsByAttActionAndEffect.get(toHashBy);

				//This effect was ruled out so continue
				if (relevantPredictions == null)  {
					continue;
				}

				//If already a prediction for this, update the condition and verify that there are no overlaps
				Prediction prediction;
				if ((prediction = predThatPredictsThisEffect(relevantPredictions, observedEffect)) != null) {

					prediction.updateConditionLearners(s, sPrime, true);

					//Verify that there are no overlaps
					if (this.predictionsOverlap(prediction, relevantPredictions)) {
						relevantPredictions = null;
					}
				}

				//If we observed an effect that we had no prediction for then add this prediction and update the learner for it
				else {
					prediction = new Prediction(this.propFunsToUse, oClass, att, a, observedEffect, s, this.statePerceptionToUse);
					relevantPredictions.add(prediction);

					//Make sure no overlap with an existing prediction of this type
					if (this.predictionsOverlap(prediction, relevantPredictions)) {
						relevantPredictions = null;
					}
					//make sure there aren't more than K predictions
					else if (relevantPredictions.size() > this.k) {
						relevantPredictions = null;
					}
				}


				predictionsThatPredictEffectsForState.add(prediction);

				this.predictionsByAttActionAndEffect.put(toHashBy, relevantPredictions);
			}


		}
		//Lastly update false conditions for all other conditionlearners -- for classic DOORMAX this will do nothing
		List<Prediction> allPredictions = this.getAllPredictionsByClassEtc(oClass, att, a);
		for (Prediction currPred : allPredictions) {
			if (!predictionsThatPredictEffectsForState.contains(currPred)) {
				currPred.updateConditionLearners(s, sPrime, false);
			}
		}

	}

	public List<Prediction> getAllPredictions() {

		List<Prediction> allPredictions = new ArrayList<Prediction>();
		for (List<Prediction> preds : this.predictionsByAttActionAndEffect.values()) {
			if (preds != null) {
				allPredictions.addAll(preds);
			}
		}
		return allPredictions;
	}
	private List<Prediction> getAllPredictionsByClassEtc(ObjectClass oClass, Attribute att, GroundedAction ga) {
		List<Prediction> allPredictions = new ArrayList<Prediction>();
		for (String eTypeString : this.effectsToUse) {
			AttActionEffectTuple toHashBy = new AttActionEffectTuple(oClass, att, ga, eTypeString);
			List<Prediction> relevantPredictions = this.predictionsByAttActionAndEffect.get(toHashBy);
			if (relevantPredictions != null) {
				allPredictions.addAll(relevantPredictions);
			}
		}
		return allPredictions;
	}

	private boolean effectIsIncompatible(List<Effect> effects, State state, Effect eToTest) {
		DiscreteStateHashFactory hf = new DiscreteStateHashFactory();
		for (Effect e : effects) {
			//Act on same object
			if (e.actOnTheSameObjectClassAndAttribute(eToTest)) {
				//And would cause object to have different values
				if (!hf.hashState(e.applyEffect(state)).equals(hf.hashState(eToTest.applyEffect(state)))) {
					return true;
				}
			}
		}
		return false;

	}

	/**
	 * 
	 * @param s state to check if a no op for
	 * @param a action to check for no op for on s
	 * @return a boolean of if a will be a no op on s
	 */
	private boolean aNoOpCondition(State s, GroundedAction a, ObjectClass oClass, Attribute att) {
		PaperAttributeActionTuple toHashBy = new PaperAttributeActionTuple(oClass, att, a);
		List<ConditionHypothesis> failureConditions = this.failureConditionsByActionOClassAtt.get(toHashBy);
		ConditionHypothesis currStateCondHyp = new ConditionHypothesis(s, this.propFunsToUse);
		for (ConditionHypothesis currHyp : failureConditions) {
			if (currHyp.matches(currStateCondHyp)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 
	 * @param s state to predict on
	 * @param a action whose result we want to predict on s
	 * @return the state predicted or null if not known
	 */
	public State predict(State s, GroundedAction a) {
		List<Effect> predEffects = predictEffects(s, a);
		if (predEffects == null) return null;
		return applyEffects(predEffects, s);
	}

	public List<Effect> predictEffectsOnAttAndOclass(State s, GroundedAction a,  ObjectClass oClass, Attribute att) {
		List<Effect> predictedEffects = new ArrayList<Effect>();
		//Check for no ops
		if (aNoOpCondition(s, a, oClass, att)) {
			return predictedEffects;
		}

		//It's not a known no op condition
		for (String effectType : this.effectsToUse) {
			AttActionEffectTuple toHashBy = new AttActionEffectTuple(oClass, att, a, effectType);

			List<Prediction> relevantPredictions = this.predictionsByAttActionAndEffect.get(toHashBy);

			//Effect type ruled out so continue
			if (relevantPredictions == null) continue; 

			//Add relevant predictions
			for (Prediction pred : relevantPredictions) {
				Effect predictedEffect = pred.predictResultingEffect(s);

				//If prediction doesn't know, return don't know
				if (predictedEffect == null) return null;

				//If prediction is non-null (i.e. condition learner predicted true)
				if (!predictedEffect.isNullEffect()) {

					//If prediction is incompatible with one of the effects return don't know
					if (effectIsIncompatible(predictedEffects, s, predictedEffect)) {
						return null;
					}

					//If prediction's effect is redundant skip it
					if (effectIsRedundant(predictedEffects, predictedEffect)) {
						continue;
					}

					//Add predicted effect
					predictedEffects.add(predictedEffect);

				}
			}

		}
		//If no effects then return don't know
		if (predictedEffects.size() == 0) return null;

		return predictedEffects;
	}

	public List<Effect> predictEffects(State s, GroundedAction ga) {
		List<Effect> toReturn = new ArrayList<Effect>();

		for (ObjectClass oClass : this.d.getObjectClasses()) {
			for (Attribute att : oClass.attributeList) {
				List<Effect> effects = predictEffectsOnAttAndOclass(s, ga,  oClass, att);
				if (effects == null) {
					return null;
				}
				toReturn.addAll(effects);
			}
		}
		return toReturn;
	}

	/**
	 * Assumes none of the effects are incompatible
	 * @param effects
	 * @param eToTest
	 * @return
	 */
	private boolean effectIsRedundant(List<Effect> effects, Effect eToTest) {

		for (Effect e : effects) {
			if (effects.indexOf(e) != effects.indexOf(eToTest) && e.actOnTheSameObjectClassAndAttribute(eToTest)) {
				return true;
			}
		}
		return false;

	}

	/**
	 * Note: assumes compatible effects
	 * @param effects the effects to apply
	 * @param s state to apply effects to
	 * @return a state with each unique effect applied
	 */
	private State applyEffects(List<Effect> effects, State s) {
		State toReturn = s.copy();

		//Apply effects
		for (Effect e : effects) {
			toReturn = e.applyEffect(toReturn);
		}
		return toReturn;
	}

	@Override
	public String toString() {
		//Predictions in effect
		StringBuilder toReturn = new StringBuilder("predictionsLearner:\n");

		for (List<Prediction> preds : this.predictionsByAttActionAndEffect.values()) {
			if (preds != null) {
				for (Prediction pred : preds) {
					toReturn.append(pred + "\n");
				}
			}
		}

		//Failure conditions
		//				for (GroundedAction a : this.allGAs) {
		//		
		//					for (ObjectClass oClass : this.d.getObjectClasses()) {
		//						for (Attribute att : oClass.attributeList) {
		//							PaperAttributeActionTuple toHashBy = new PaperAttributeActionTuple(oClass, att, a);
		//							List<ConditionHypothesis> failureConditionsForAction = this.failureConditionsByActionOClassAtt.get(toHashBy);
		//							if (failureConditionsForAction.size() == 0) continue;
		//							toReturn.append("\tFailure condition for " + a +  " on " + oClass.name + "'s " + att.name + ": ");
		//		
		//							for (ConditionHypothesis cond : failureConditionsForAction) {
		//								toReturn.append(cond);
		//		
		//							}
		//							toReturn.append("\n");
		//						}
		//					}
		//		
		//		
		//					toReturn.append("\n");
		//		
		//				}

		return toReturn.toString();
	}


}
