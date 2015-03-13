package burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel;

import java.util.List;

import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects.Effect;
import burlap.behavior.singleagent.learning.modellearning.rmax.TaxiDomain;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.Value;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;

/**
 * Wrapper class which given a particular effect, instantiates a condition learner to learn the conditions under which that effect occurs
 * @author Dhershkowitz
 *
 */
public class Prediction {

	private ConditionLearner CL;
	private Effect effectToLearnConditionFor;
	private List<PropositionalFunction> propFuns;
	private ObjectClass associatedOClass;
	private Attribute relevantAtt;
	private GroundedAction associatedAction;

	/**
	 * 
	 * @param propFuns prop functions for the condition learner to consider
	 * @param OC the relevant object cass
	 * @param att the relevant attribute
	 * @param act the relevant action
	 * @param effectToLearnConditionFor the effect that the CELearner is learning the condition for
	 * @param initialState the state in which the effect was first observed just before taking act
	 */
	public Prediction(List<PropositionalFunction> propFuns, ObjectClass OC, Attribute att, GroundedAction act, Effect effectToLearnConditionFor, State initialState) {

		this.propFuns = propFuns;
		this.associatedOClass = OC;
		this.relevantAtt = att;
		this.CL = new ConditionLearner();
		this.associatedAction = act;
		this.effectToLearnConditionFor = effectToLearnConditionFor;

		int [] currStateAsBitString = StateHelpers.stateToBitStringOfPreds(initialState, this.propFuns);
		this.CL.updateVersionSpace(currStateAsBitString);

	}

	/**
	 * 
	 * @return the Effect that this CELearner is learning the condition for
	 */
	public Effect getEffectLearningFor() {
		return this.effectToLearnConditionFor;
	}


	/**
	 * 
	 * @param otherCEL the CELearner to compare against
	 * @return a boolean of whether or not the conditions of the two CELearner's condition learners overlap
	 */
	public boolean conditionsOverlap(Prediction otherCEL) {
		return (this.CL.conditionsOverlap(otherCEL.CL));
	}

	public boolean predictionOn(ObjectClass oClass, Attribute att, GroundedAction ga, String effectType) {
		return this.associatedOClass.equals(oClass) && this.relevantAtt.equals(att) && this.associatedAction.equals(ga)
				&& this.effectToLearnConditionFor.getEffectTypeString().equals(effectType);
	}
	
	/**
	 * 
	 * @param s the state to predict on
	 * @return null if the condition learner predicts false, the relevant effect otherwise
	 */
	public Effect predictResultingEffect(State s) {
		int [] currStateAsBitString = StateHelpers.stateToBitStringOfPreds(s, this.propFuns);

		boolean CLPrediction = this.CL.conditionTrueInState(currStateAsBitString);

		//CL predicts false -- so it's a no op
		if (!CLPrediction) {
			return null;
		}

		//CL predicts true
		return this.effectToLearnConditionFor;
	}

	/**
	 * 
	 * @param s the initial state
	 * @param sPrime the resulting state where the effect we are learning for was observed on the relevant object class, attribute and after taking
	 * the relevant action
	 */
	public void updateLearners(State s, State sPrime) {
//		if (this.associatedOClass.name == "passenger" && (this.relevantAtt.name == TaxiDomain.XATT || this.relevantAtt.name == TaxiDomain.YATT)) {
//			System.out.println("Prediction for " + this + " being updated");
//		}
		
		
		int [] currStateAsBitString = StateHelpers.stateToBitStringOfPreds(s, this.propFuns);

		this.CL.updateVersionSpace(currStateAsBitString);
	}


	/**
	 * 
	 * @param CELearner the CELearner against which to compare
	 * @return a boolean as to whether or not the two CELearners are learning for the same effect
	 */
	public boolean learningSameEffect(Prediction CELearner) {
		return this.effectToLearnConditionFor.equals(CELearner.effectToLearnConditionFor);
	}
	
	/**
	 * 
	 * @param pred other prediction to compare against
	 * @return a boolean of if the two predictions's conditions overlap and they act on the same object types
	 */
	public boolean overlapWithPrediction(Prediction pred) {
		return this.effectToLearnConditionFor.actOnTheSameObjectClassAndAttribute(pred.getEffectLearningFor())
				&& this.conditionsOverlap(pred);
	}

	@Override
	public boolean equals(Object object) {
		return object == this;
	}

	@Override
	public String toString() {
		return "\tPrediction for " + this.associatedAction.actionName() + "'s effect on " + this.relevantAtt.name + " of " + this.associatedOClass.name + 
				"\n\t\tcondition:" + this.CL.getHSubT() +
				"\n\t\teffectLearner: " + this.effectToLearnConditionFor;
	}
}




