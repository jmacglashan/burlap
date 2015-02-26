package burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects.Effect;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects.EffectHelpers;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects.EffectNotFoundException;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.ObjectClass;
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

	private HashMap<GroundedAction, List<ConditionHypothesis>> failureConditionsByAction;
	private HashMap<GroundedAction, HashMap<String, List<Prediction>>> CELearnersByActionThenEffect;
	private List<String> effectsToUse;
	private Domain d;
	private int k;
	private List<PropositionalFunction> propFunsToUse;
	
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
	public PredictionsLearner(Domain d, List<PropositionalFunction> propFuns, List<String> effectsToUse, List<Action> actionsToUse, State initialState, int k) {
		this.d = d;
		this.k = k;
		this.propFunsToUse = propFuns;
		this.allGAs = new ArrayList<GroundedAction>();
		for (Action a : actionsToUse) {
			this.allGAs.addAll(a.getAllApplicableGroundedActions(initialState));
		}
		this.effectsToUse = effectsToUse;
		
		//Set up HM for failure conditions
		this.failureConditionsByAction = new HashMap<GroundedAction, List<ConditionHypothesis>>();
		for (GroundedAction a : this.allGAs) {
			this.failureConditionsByAction.put(a, new ArrayList<ConditionHypothesis>());
		}

		//Set up HM for predictions
		this.CELearnersByActionThenEffect = new HashMap<GroundedAction, HashMap<String, List<Prediction>>>();
		for (GroundedAction a : this.allGAs) {
			HashMap<String, List<Prediction>> toAdd = new HashMap<String, List<Prediction>>();

			for (String effectString : this.effectsToUse) {
				List<Prediction> CELearners = new ArrayList<Prediction>();
				toAdd.put(effectString, CELearners);
			}

			this.CELearnersByActionThenEffect.put(a, toAdd);
		}	
	}

	/**
	 * 
	 * @param pred to check for overlaps with
	 * @param relevantPredictions a list of predicates to check if pred overlaps against
	 * @return a boolean of if pred's conditon overlaps with a condition in relevantLearners (other than itself)
	 */
	private boolean areOverlapsOfConditions(Prediction pred, List<Prediction> relevantPredictions) {
		for (Prediction otherPred : relevantPredictions) {
			if (relevantPredictions.indexOf(pred) != relevantPredictions.indexOf(otherPred) && pred.conditionsOverlap(otherPred)) {
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

	/**
	 * 
	 * @param s a state
	 * @param a an action
	 * @param sPrime the state that results from a on s
	 */
	public void learn(State s, GroundedAction a, State sPrime) {
		//Found a failure condition for an action -- update as necessary
		if (s.equals(sPrime)) {
			ConditionHypothesis failureHyp = new ConditionHypothesis(s, this.propFunsToUse);

			List<ConditionHypothesis> failureConditions = this.failureConditionsByAction.get(a);
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

			HashMap<String, List<Prediction>> toUpdate = this.CELearnersByActionThenEffect.get(a);

			for (ObjectClass oClass : this.d.getObjectClasses()) {
				for (Attribute at : oClass.attributeList) {

					//Get possible effects for this oClass and attribute
					List<Effect> possibleEffects = null;
					try {
						possibleEffects = EffectHelpers.getPossibleEffects(s, sPrime, oClass, at, this.effectsToUse);
					} catch (EffectNotFoundException e) {
						e.printStackTrace();
					}
					for (Effect observedEffect: possibleEffects) {
						List<Prediction> relevantPredictions = toUpdate.get(observedEffect.getEffectTypeString());
						//This effect was ruled out so continue
						if (relevantPredictions == null)  {
							continue;
						}

						//If already a prediction for this, update the condition and verify that there are no overlaps
						Prediction CELearner;
						if ((CELearner = predThatPredictsThisEffect(relevantPredictions, observedEffect)) != null) {
							CELearner.updateLearners(s, sPrime);

							//Verify that there are no overlaps
							if (this.areOverlapsOfConditions(CELearner, relevantPredictions)) {
								relevantPredictions = null;
							}
						}

						//If we observed an effect that we had no prediction for then add this prediction and update the learner for it
						else {
							Prediction toAdd = new Prediction(this.propFunsToUse, oClass, at, a, observedEffect, s);
							relevantPredictions.add(toAdd);

							//Make sure no overlap with an existing prediction of this type
							if (this.areOverlapsOfConditions(toAdd, relevantPredictions)) {
								relevantPredictions = null;
							}
							//make sure there aren't more than K predictions
							else if (relevantPredictions.size() > this.k) {
								relevantPredictions = null;
							}
						}
						toUpdate.put(observedEffect.getEffectTypeString(), relevantPredictions);
					}
				}
			}
			this.CELearnersByActionThenEffect.put(a, toUpdate);
		}
	}


	/**
	 * 
	 * @param effects a list of effects
	 * @return a boolean of if any of the effects are incompatible -- i.e. act on the same object class and attributes
	 */
	private boolean incompatibleEffects(List<Effect> effects) {
		for (Effect e : effects) {
			for (Effect otherE : effects) {
				if (effects.indexOf(e)!= effects.indexOf(otherE) && e.actOnTheSameObjectClassAndAttribute(otherE)) {
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
	private boolean aNoOpCondition(State s, GroundedAction a) {
		List<ConditionHypothesis> failureConditions = this.failureConditionsByAction.get(a);
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

		State toReturn = s.copy();

		//Check for no ops
		if (aNoOpCondition(s, a)) {
			return toReturn;
		}

		//It's not a known no op condition
		HashMap<String, List<Prediction>> effectTypeToLearners = this.CELearnersByActionThenEffect.get(a);

		List<Effect> predictedEffects = new ArrayList<Effect>();

		for (String effectTypeString : this.effectsToUse) {
			List<Prediction> predictions = effectTypeToLearners.get(effectTypeString);
			if (predictions == null) continue;//Effect ruled out

			for (Prediction pred : predictions) {
				Effect predictedEffect = pred.predictResultingEffect(s);
				//If one doesn't know, then don't know
				if (predictedEffect != null) {
					predictedEffects.add(predictedEffect);
				}
				//Otherwise add to list of predicted effects
			}
		}

		//If no effects then return a no-op
		if (predictedEffects.size() == 0) return null;

		//If incompatible effects return don't know
		if (incompatibleEffects(predictedEffects)) {
			return null;
		}

		//Apply effects and return result
		for (Effect e : predictedEffects) {
			toReturn = e.applyEffect(toReturn);
		}
		return toReturn;

	}

	@Override
	public String toString() {
		//Predictions in effect
		StringBuilder toReturn = new StringBuilder("predictionsLearner:\n");
		for (GroundedAction a : this.allGAs) {
			HashMap<String, List<Prediction>> forActions = this.CELearnersByActionThenEffect.get(a);

			for (String effectString : this.effectsToUse) {

				List<Prediction> predictions = forActions.get(effectString);
				if (predictions != null) {
					for (Prediction pred : predictions) {
						toReturn.append(pred + "\n");
					}
				}
			}
		}

		//Failure conditions
		for (GroundedAction a : this.allGAs) {

			List<ConditionHypothesis> failureConditionsForAction = this.failureConditionsByAction.get(a);
			if (failureConditionsForAction.size() == 0) continue;
			toReturn.append("\tFailure condition for " + a + ": ");
			for (ConditionHypothesis failureCond : failureConditionsForAction) {
				toReturn.append(failureCond);
			}

			toReturn.append("\n");

		}

		return toReturn.toString();
	}


}
