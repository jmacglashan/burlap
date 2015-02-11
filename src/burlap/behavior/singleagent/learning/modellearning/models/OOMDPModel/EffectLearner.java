package burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects.ArithmeticEffect;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects.AssigmentEffect;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects.Effect;
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

	boolean allEffectsRuledOut;

	public EffectLearner(int numPreds,  ObjectClass oClass, Attribute att){
		this.relevantObjectClass = oClass;
		this.relevantAtt = att;
		allEffectsRuledOut = false;
	}

	public void updateVersionSpace(State s, State sPrime) {
		List<Effect> possibleEffects = getPossibleEffects(s, sPrime);
		//Initialize HHat if it's empty
		if (HHat == null) {
			this.HHat = new HashSet<Effect>();
			for (Effect possEffect: possibleEffects) {
				HHat.add(possEffect);
			}
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

				for(Effect possEffect: possibleEffects) {
					if (possEffect.equals(hypEffect)) {
						wasEqualToOne = true;
					}
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

	public Effect computePrediction(State state) {
		List<State> resultingStates = new ArrayList<State>();

		//No positive instances so don't know
		if (HHat == null) return null;
		
		//Get hypothesized resulting states
		for (Effect hypEffect : HHat) {
			State hypState = hypEffect.applyEffect(state);
			resultingStates.add(hypState);
		}

		//Check if resulting states contradict If they do return don't know
		if (!resultingStates.isEmpty()) {
			State firstState = resultingStates.get(0);
			for (int index = 1; index < resultingStates.size(); index++) {
				if (!firstState.equals(resultingStates.get(index))) {//JAMES IS THIS OK?
					return null; //States not equal so contradicting effects so return don't know
				}
			}	
		}

		//If they don't return some effect
		for (Effect hypEffect : HHat) {
			return hypEffect;
		}

		//All effects ruled out so best we can possibly guess is a no-op
		return new NullEffect(this.relevantObjectClass, this.relevantAtt);
	}


	public List<Effect> getPossibleEffects(State s, State sPrime) {
		List<Effect> toReturn = new ArrayList<Effect>();


		for (ObjectInstance o: s.getObjectsOfTrueClass(this.relevantObjectClass.name)) {

			String objectName = o.getName();

			//Find object of Same Name
			ObjectInstance oInSPrime = sPrime.getObject(objectName);

			//If object was deleted keep on truckin'
			if (oInSPrime == null) {
				continue;
			}

			//Check if attribute value has changed
			Value attValBefore = o.getValueForAttribute(this.relevantAtt.name);
			Value attValAfter = oInSPrime.getValueForAttribute(this.relevantAtt.name);

			if (!attValBefore.equals(attValAfter)){
				double numValBefore = attValBefore.getNumericRepresentation();
				double numValAfter = attValAfter.getNumericRepresentation();

				//Hypothesize effects
				ArithmeticEffect arithEffect = new ArithmeticEffect(this.relevantObjectClass,this.relevantAtt,numValAfter - numValBefore);

				AssigmentEffect assEffect = new AssigmentEffect(this.relevantObjectClass, this.relevantAtt, numValAfter);

				toReturn.add(arithEffect);
				toReturn.add(assEffect);
				break; // ASSUMING A SINGLE CHANGE RIGHT NOW

			}
		}
		return toReturn;
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
