package burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel;

import java.util.List;

import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects.Effect;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.Value;
import burlap.oomdp.singleagent.Action;

public class ConditionEffectLearner {

	private ConditionLearner CL;
	
	private EffectLearner EL;
	
	public List<PropositionalFunction> propFuns;
	private ObjectClass associatedOClass;
	private Attribute relevantAtt;
	private Action associatedAction;
	
	public ConditionEffectLearner(Domain domain, ObjectClass OC, Attribute att, Action act) {
		this.propFuns = domain.getPropFunctions();
		this.associatedOClass = OC;
		this.relevantAtt = att;
		this.CL = new ConditionLearner(propFuns.size());
		this.EL = new EffectLearner(propFuns.size(), OC, att);	
		this.associatedAction = act;
	}
	
	public State predictResultingState (State s) {
		int [] currStateAsBitString = StateHelpers.stateToBitStringOfPreds(s, this.propFuns);
		
		List<Boolean> CLPrediction = this.CL.computePredictions(currStateAsBitString);
		
		boolean CLKnows = CLPrediction.size() == 1;
		
		//CL predicts false -- so it's a no op
		if (CLKnows && !CLPrediction.get(0)) {
			return s;
		}
		
		//CL predicts true
		if (CLKnows && CLPrediction.get(0)) {
			
			Effect predictedEffect = this.EL.computePrediction(s);
			
			//If effect learner doesn't know then CE learner doesn't
			if (predictedEffect == null) {
				return null;
			}
			
			//If effect learner knows then return resulting state
			return predictedEffect.applyEffect(s);
			
		}
		
		//Don't know
		return null;
	}
	
	public void updateLearners(State s, State sPrime) {
		int [] currStateAsBitString = StateHelpers.stateToBitStringOfPreds(s, this.propFuns);
				
		boolean conditionWasTrue = conditionWasMet(s, sPrime);
		
		//Only update effects learner if condition was true
		if (conditionWasTrue) {
			this.EL.updateVersionSpace(s, sPrime);	
		}
		
		//Always update effects learner
		this.CL.updateVersionSpace(conditionWasTrue, currStateAsBitString);
	}
	
	private boolean conditionWasMet(State s, State sPrime) {
		List<ObjectInstance> relevantObjects = s.getObjectsOfTrueClass(this.associatedOClass.name);
		
		for (ObjectInstance o: relevantObjects) {
			Value beforeVal = o.getValueForAttribute(this.relevantAtt.name);
			Value afterVal = sPrime.getObject(o.getName()).getValueForAttribute(this.relevantAtt.name);
			//One of the values changed
			if (!beforeVal.equals(afterVal)) {
				return true;
			}
		}
		
		return false;
	}
	

	
	public String knowledgeOnState(State s) {
		int [] stateAsBitString = StateHelpers.stateToBitStringOfPreds(s, this.propFuns);
		
		return "\tCEL for " + this.associatedAction.getName() + "'s effect on " + this.relevantAtt.name + " of " + this.associatedOClass.name + 
				"\n\t\tconditionLearner: prediction for condition is " + this.CL.computePredictions(stateAsBitString) + " HSubT is " + this.CL.HSubT + " and observations are " + this.CL.getObservedStatePreds() + 
				"\n\t\teffectLearner: " + this.EL.computePrediction(s);
	}
	
	@Override
	public String toString() {
		return "\tCEL for " + this.associatedAction.getName() + "'s effect on " + this.relevantAtt.name + " of " + this.associatedOClass.name + 
				"\n\t\tconditionLearner: prediction for condition is " + this.CL.getObservedStatePreds() + " HSubT is " + this.CL.HSubT + "and hypotheses are " + this.CL.hypothesesToString() +
				"\n\t\teffectLearner: " + this.EL.toString();
	}
}




