package burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.Value;

public class EffectHelpers {
	public static String arithEffect = "arithemeticEffect";
	public static String assigEffect = "assigmentEffect";


	//	public static Effect instantiateEffectByName(String name, ObjectClass oClass, Attribute at, int val) throws EffectNotFoundException {
	//		//Arithmetic effect
	//		if (name.equals(arithEffect)) {
	//			return new ArithmeticEffect(oClass, at, val);
	//		}
	//		else if (name.equals(assigEffect)) {
	//			return new AssigmentEffect(oClass, at, val);
	//		}
	//		
	//		throw new EffectNotFoundException();
	//		
	//	}

	public static List<String> effectsToUse() {
		List<String> toReturn = new ArrayList<String>();

		toReturn.add(arithEffect);
		toReturn.add(assigEffect);

		return toReturn;
	}

	public static Effect getPossibleEffect(State s, State sPrime, ObjectClass relevantObjectClass, Attribute relevantAtt, String effectTypeString) {


		for (ObjectInstance o: s.getObjectsOfTrueClass(relevantObjectClass.name)) {

			String objectName = o.getName();

			//Find object of Same Name
			ObjectInstance oInSPrime = sPrime.getObject(objectName);

			//If object was deleted keep on truckin'
			if (oInSPrime == null) {
				continue;
			}

			//Check if attribute value has changed
			Value attValBefore = o.getValueForAttribute(relevantAtt.name);
			Value attValAfter = oInSPrime.getValueForAttribute(relevantAtt.name);

			if (!attValBefore.equals(attValAfter)){
				double numValBefore = attValBefore.getNumericRepresentation();
				double numValAfter = attValAfter.getNumericRepresentation();

				//Hypothesize effects
				if  (effectTypeString.equals(arithEffect)) {
					return new ArithmeticEffect(relevantObjectClass,relevantAtt,numValAfter - numValBefore);

				}

				else if (effectTypeString.equals(assigEffect)) {
					return new AssigmentEffect(relevantObjectClass, relevantAtt, numValAfter);

				}

			}
		}
		return null;
	}

}
