package burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effect.ArithmeticEffect;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effect.AssigmentEffect;
import burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effect.Effect;
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
		//All effects ruled out
		else if (HHat.size() == 0) {
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
	
	public State computePredictions(State state) {
		List<State> resultingStates = new ArrayList<State>();
		
		//Get hypothesized resulting states
		for (Effect hypEffect : HHat) {
			State hypState = hypEffect.applyEffect(state);
			resultingStates.add(hypState);
		}
		
		//Check if resulting states contradict If they do return don't know
		int index = 0;
		for (State possState : resultingStates) {
			if (!possState.equals(resultingStates.get(index - 1))) {
				return null;
			}
			index += 1;
			
			//JAMES IS THIS OK?
		}
		
		//If they don't return the state
		
		return resultingStates.get(0);
			
		
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
	
}
