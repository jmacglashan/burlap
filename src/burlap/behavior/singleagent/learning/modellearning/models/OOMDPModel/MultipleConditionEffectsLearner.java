package burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects.Effect;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects.EffectHelpers;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.core.Attribute;

public class MultipleConditionEffectsLearner {

	private HashMap<Action, List<ConditionHypothesis>> failureConditionsByAction;
	private HashMap<Action, HashMap<String, List<ConditionEffectLearner>>> CELearnersByActionThenEffect;
	private List<String> effectsToUse;
	private Domain d;
	private int k = 5;
	private List<PropositionalFunction> propFunsToUse;
	
	public MultipleConditionEffectsLearner(Domain d, List<PropositionalFunction> propFunToUse) {
		this.d = d;
		this.propFunsToUse = propFunToUse;
		List<Action> actions = d.getActions();
		List<ObjectClass> oClasses = d.getObjectClasses();
		this.effectsToUse = EffectHelpers.effectsToUse();
		this.failureConditionsByAction = new HashMap<Action, List<ConditionHypothesis>>();
		for (Action a : actions) {
			this.failureConditionsByAction.put(a, new ArrayList<ConditionHypothesis>());
		}

		this.CELearnersByActionThenEffect = new HashMap<Action, HashMap<String, List<ConditionEffectLearner>>>();

		//Instantiate CELearners for all actions attribute-object class -- effect triples

		for (Action a : actions) {
			HashMap<String, List<ConditionEffectLearner>> toAdd = new HashMap<String, List<ConditionEffectLearner>>();
			for (ObjectClass oClass : oClasses) {
				for (Attribute att : oClass.attributeList) {
					for (String effectString : this.effectsToUse) {
						List<ConditionEffectLearner> CELearners = new ArrayList<ConditionEffectLearner>();
						toAdd.put(effectString, CELearners);
					}
				}
			}
			this.CELearnersByActionThenEffect.put(a, toAdd);
		}	
	}

	public boolean areOverlapsOfConditions(ConditionEffectLearner CELearner, List<ConditionEffectLearner> relevantLearners) {
		boolean areOverlaps = false;

		for (ConditionEffectLearner otherCELearner : relevantLearners) {
			if (relevantLearners.indexOf(CELearner) != relevantLearners.indexOf(otherCELearner) && CELearner.conditionsOverlap(otherCELearner)) {
				areOverlaps = true;
				//System.out.println("Overlap between\n " + currCELearner + " and \n" + otherCELearner);
				//System.out.println("\tIndices: " + relevantCELearners.indexOf(currCELearner) + " and " + relevantCELearners.indexOf(otherCELearner));
			}
		}

		return areOverlaps;
	}

	public ConditionEffectLearner CELearnerThatPredictsThisEffect(List<ConditionEffectLearner> relevantCELearners, Effect observedEffect) {
		for (ConditionEffectLearner CELearner : relevantCELearners) {
			if (CELearner.EL.identicalEffectPredicted(observedEffect)) {
				//				System.out.println("Effect already predicted: " + observedEffect );
				return CELearner;
			}
		}
		return null;

	}

	public void learn(State s, Action a, State sPrime) {
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
		//Wasn't a failure condition -- update learners and stuff
		else {
			HashMap<String, List<ConditionEffectLearner>> toUpdate = this.CELearnersByActionThenEffect.get(a);


			for (ObjectClass oClass : this.d.getObjectClasses()) {
				for (Attribute at : oClass.attributeList) {
					for (String effectString: effectsToUse) {
						Effect observedEffect = EffectHelpers.getPossibleEffect(s, sPrime, oClass, at, effectString);
						if (observedEffect == null) continue;
//						System.out.println("Testing effect: " + observedEffect + " for action " + a.getName());

						List<ConditionEffectLearner> relevantCELearners = toUpdate.get(effectString);

						//This effect was ruled out so continue
						if (relevantCELearners == null)  {
//							System.out.println("Effect already ruled out: " + observedEffect);
							continue;
						}


						//If already a prediction for this, update the condition and verify that there are no overlaps
						ConditionEffectLearner CELearner;
						if ((CELearner = CELearnerThatPredictsThisEffect(relevantCELearners, observedEffect)) != null) {
//							System.out.println("Effect already predicted: " + observedEffect );
							CELearner.updateLearners(s, sPrime);

							//Verify that there are no overlaps
							if (this.areOverlapsOfConditions(CELearner, relevantCELearners)) {
//								System.out.println("Eliminating" + observedEffect + " of similar types for " + a.getName());
								relevantCELearners = null;
							}
						}

						//If we observed an effect that we had no prediction for then add this prediction and update the learner for it
						else {
//							System.out.println("New learner: " + observedEffect +  " for " + a.getName());
							ConditionEffectLearner toAdd = new ConditionEffectLearner(this.d, oClass, at, a, effectString);
							toAdd.updateLearners(s, sPrime);
							relevantCELearners.add(toAdd);

							//Make sure no overlap with an existing prediction of this type
							if (this.areOverlapsOfConditions(toAdd, relevantCELearners)) {
//								System.out.println("Eliminating" + observedEffect + " of similar types for " + a.getName());
								relevantCELearners = null;
							}

							//make sure there aren't more than K predictions
							else if (relevantCELearners.size() > this.k) {
//								System.out.println("K exceeded -- eliminating" + observedEffect + " of similar types for " + a.getName());
								relevantCELearners = null;
							}
						}

						toUpdate.put(effectString, relevantCELearners);



					}
				}

			}

			this.CELearnersByActionThenEffect.put(a, toUpdate);
		}
	}


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
	
	private boolean aNoOpCondition(State s, Action a) {
		List<ConditionHypothesis> failureConditions = this.failureConditionsByAction.get(a);
		ConditionHypothesis currStateCondHyp = new ConditionHypothesis(s, this.propFunsToUse);
		
		for (ConditionHypothesis currHyp : failureConditions) {
			if (currHyp.matches(currStateCondHyp)) {
				return true;
			}
		}
		
		return false;
	}

	public State predict(State s, Action a) {
		//System.out.println(this.predictionsStillInEffect());
		//System.out.println(this.stateOfEffectsOnState(s));

		State toReturn = s.copy();

		//Check for no ops
		if (aNoOpCondition(s, a)) {
			return toReturn;
		}
		
		//It's not a known no op condition
		HashMap<String, List<ConditionEffectLearner>> effectTypeToLearners = this.CELearnersByActionThenEffect.get(a);

		List<Effect> predictedEffects = new ArrayList<Effect>();

		for (String effectTypeString : this.effectsToUse) {
			List<ConditionEffectLearner> CELearners = effectTypeToLearners.get(effectTypeString);
			if (CELearners == null) continue;//Effect ruled out

			for (ConditionEffectLearner CELearner : CELearners) {
				Effect predictedEffect = CELearner.predictResultingEffect(s);
				//If one doesn't know, then don't know
				if (predictedEffect == null) {
					//System.out.println("Didn't know");
					return null;
				}
				//Otherwise add to list of predicted effects
				predictedEffects.add(predictedEffect);
			}
		}
		//System.out.println("predictedEffects: " + predictedEffects);

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

	public String predictionsStillInEffect() {
		StringBuilder toReturn = new StringBuilder("predictionsInEffect:\n");
		for (Action a : d.getActions()) {
			HashMap<String, List<ConditionEffectLearner>> forActions = this.CELearnersByActionThenEffect.get(a);

			for (String effectString : this.effectsToUse) {

				List<ConditionEffectLearner> CELearners = forActions.get(effectString);
				if (CELearners != null) {
					for (ConditionEffectLearner CELearner : CELearners) {
						toReturn.append(CELearner + "\n");
					}
				}

			}
		}
		
		for (Action a : d.getActions()) {
			toReturn.append("Failure condition for " + a.getName() + ": ");
			List<ConditionHypothesis> failureConditionsForAction = this.failureConditionsByAction.get(a);
			for (ConditionHypothesis failureCond : failureConditionsForAction) {
				toReturn.append(failureCond);
			}
			
			
			toReturn.append("\n");
			
		}

		return toReturn.toString();
	}

	public String stateOfEffectsOnState(State state) {
		StringBuilder toReturn = new StringBuilder();

		for (Action action : this.d.getActions()) {
			HashMap<String, List<ConditionEffectLearner>> effectTypeToLearners = this.CELearnersByActionThenEffect.get(action);


			List<ConditionEffectLearner> relevantLearners = new ArrayList<ConditionEffectLearner>();
			for (List<ConditionEffectLearner> CELearners : effectTypeToLearners.values()) {
				relevantLearners.addAll(CELearners);
			}

			if (relevantLearners.size() == 0) {
				return "No relevant learners";
			}


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
		}
		return toReturn.toString();

	}

	public String stateOfEffectsOnStateByAction(State state, Action action) {
		StringBuilder toReturn = new StringBuilder();

		HashMap<String, List<ConditionEffectLearner>> effectTypeToLearners = this.CELearnersByActionThenEffect.get(action);


		List<ConditionEffectLearner> relevantLearners = new ArrayList<ConditionEffectLearner>();
		for (List<ConditionEffectLearner> CELearners : effectTypeToLearners.values()) {
			relevantLearners.addAll(CELearners);
		}

		if (relevantLearners.size() == 0) {
			return "No relevant learners";
		}


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
