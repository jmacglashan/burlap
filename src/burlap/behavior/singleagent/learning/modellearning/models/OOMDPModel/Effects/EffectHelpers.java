package burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.Value;

/**
 * Class with static methods to aid in effect functionality -- namely extracting possible effects from a state and the resulting state and providing
 * a namespace for effects
 * @author Dhershkowitz
 *
 */
public class EffectHelpers {

	//Namespace for effects by string name
	public static String arithEffect = "arithmeticEffect";
	public static String assigEffect = "assignmentEffect";

	/**
	 * Helper method to turn Values before and after into effects
	 * @param name name of the effect to be instantiated
	 * @param oClass objectClass to check for effect on
	 * @param at Attribute to check for effect on
	 * @param valueBefore the Value of the effect before (i.e. in s)
	 * @param valueAfter the Value of the effect after (i.e. in sPrime)
	 * @return the effect to be instantiated
	 * @throws EffectNotFoundException
	 */
	private static Effect instantiateEffectByName(String name, ObjectClass oClass, Attribute at, Value valueBefore, Value valueAfter) throws EffectNotFoundException {
		//Arithmetic effect
		if (name.equals(arithEffect)) {
			return new ArithmeticEffect(name, oClass, at, valueBefore, valueAfter);
		}

		//Assignment effect
		else if (name.equals(assigEffect)) {
			return new AssignmentEffect(name, oClass, at, valueAfter);
		}

		throw new EffectNotFoundException(name);
	}

	/**
	 * 
	 * @param s the initial state
	 * @param sPrime the state after some action is performed
	 * @param relevantObjectClass the objectClass to check for changes in
	 * @param relevantAtt the attribute to check for changes in
	 * @param effectsToUse a list of string names of effects to check for
	 * @return a list of possible effects from effectsToUse that could explain the transition from s to sPrime
	 * @throws EffectNotFoundException
	 */
	public static List<Effect> getPossibleEffects(State s, State sPrime, ObjectClass relevantObjectClass, Attribute relevantAtt, List<String> effectsToUse) throws EffectNotFoundException {

		List<Effect> toReturn = new ArrayList<Effect>();

		for (ObjectInstance o: s.getObjectsOfTrueClass(relevantObjectClass.name)) {

			String objectName = o.getName();

			//Find object of same name
			ObjectInstance oInSPrime = sPrime.getObject(objectName);

			//If object was deleted keep on truckin'
			if (oInSPrime == null) {
				continue;
			}

			//Check if attribute value has changed
			Value attValBefore = o.getValueForAttribute(relevantAtt.name);
			Value attValAfter = oInSPrime.getValueForAttribute(relevantAtt.name);

			if (!attValAfter.equals(attValBefore)) {
				//Hypothesize effects
				for (String effectTypeString : effectsToUse) {
					toReturn.add(instantiateEffectByName(effectTypeString, relevantObjectClass, relevantAtt, attValBefore, attValAfter));
				}
			}
		}
		return toReturn;
	}

}
