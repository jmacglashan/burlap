package burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects.ArithmeticEffect;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects.AssigmentEffect;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects.Effect;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects.EffectHelpers;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects.NullEffect;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.Value;

public class EffectLearner {
	public HashSet<Effect> HHat;

	private Attribute relevantAtt;
	private ObjectClass relevantObjectClass;
	private String effectType;
	
	boolean allEffectsRuledOut;

	public EffectLearner(int numPreds,  ObjectClass oClass, Attribute att, String effectType){
		this.relevantObjectClass = oClass;
		this.relevantAtt = att;
		allEffectsRuledOut = false;
		this.effectType = effectType;
	}

	public void updateVersionSpace(State s, State sPrime) {
		Effect possibleEffect = EffectHelpers.getPossibleEffect(s, sPrime, this.relevantObjectClass, this.relevantAtt, this.effectType);
		//Initialize HHat if it's empty
		if (HHat == null) {
			this.HHat = new HashSet<Effect>();
				HHat.add(possibleEffect);
		}
		//All effects ruled out -- must be a no op so far as we know
		else if (HHat.size() == 0) {
			HHat.add(new NullEffect(this.relevantObjectClass, this.relevantAtt));
			return;
		}

		//Otherwise rule out other hypotheses that don't work
		else {
			List<Effect> contradictoryEffects = new ArrayList<Effect>();
			for (Effect hypEffect: HHat) {
				boolean wasEqualToOne = false;

					if (possibleEffect.equals(hypEffect)) {
						wasEqualToOne = true;
				}

				if (!wasEqualToOne) {
					contradictoryEffects.add(hypEffect);
				}
			}

			//Actually remove those that were found to be contradictory
			for (Effect toRemove: contradictoryEffects) {
				HHat.remove(toRemove);
			}
		}
	}

	public boolean identicalEffectPredicted(Effect otherEffect) {
		if (this.HHat != null && this.HHat.size() == 1) {
			for (Effect hypEffect : HHat) {
				return hypEffect.equals(otherEffect);
			} 
		}
		
		
		return false;
	}
	
	public Effect computePrediction(State state) {
		List<State> resultingStates = new ArrayList<State>();

		//No positive instances so don't know
		if (HHat == null) return null;
		
		if (HHat.size() == 1) {
			for (Effect hypEffect : HHat) {
				return hypEffect;
			}
		}
		
		//Get hypothesized resulting states
		for (Effect hypEffect : HHat) {
			State hypState = hypEffect.applyEffect(state);
			resultingStates.add(hypState);
		}

		//Check if resulting states contradict if they do return don't know
//		if (!resultingStates.isEmpty()) {
//			State firstState = resultingStates.get(0);
//			for (int index = 1; index < resultingStates.size(); index++) {
//				if (!firstState.equals(resultingStates.get(index))) {//JAMES IS THIS OK?
//					return null; //States not equal so contradicting effects so return don't know
//				}
//			}	
//		}

		//If they don't return some effect
//		for (Effect hypEffect : HHat) {
//			return hypEffect;
//		}

		//All effects ruled out so best we can possibly guess is a no-op
		return new NullEffect(this.relevantObjectClass, this.relevantAtt);
	}



	
	@Override
	public String toString() {
		StringBuilder toReturn = new StringBuilder();
		toReturn.append("Possible effects are ");

		if (this.HHat != null) {
			for (Effect e : this.HHat) {
				String effect = null;
				if (e != null){
					effect = e.toString();
				}
				toReturn.append(effect);
			}
		}



		return toReturn.toString();

	}

}
