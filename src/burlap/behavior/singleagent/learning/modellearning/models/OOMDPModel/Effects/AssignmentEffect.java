package burlap.behavior.singleagent.learning.modellearning.models.OOMDPModel.Effects;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.Value;

/**
 * Class to model assigment effects under the OOMDP model -- i.e. those that assign some real value to some attribute of some
 *  object class
 * @author Dhershkowitz
 *
 */
public class AssignmentEffect extends Effect {

	private double valueToSet;

	/**
	 * 
	 * @return the value that this assigmentEffect assigns
	 */
	public double getValueToSet() {
		return this.valueToSet;
	}
	
	/**
	 * @param effectTypeString the string name of the effect type
	 * @param oClassEffected the ObjectClass that this ArithmeticEffect acts on
	 * @param atEffected the Attribute that this ArithmeticEffect acts on
	 * @param valueBefore the value of the class/att in s
	 * @param valueAfter the value of the class/att in sPrime
	 */
	public AssignmentEffect(String effectTypeString, ObjectClass oClassEffected, Attribute atEffected, Value valueAfter) {
		super(effectTypeString, oClassEffected, atEffected);
		this.valueToSet = valueAfter.getNumericRepresentation();
	}

	
	@Override
	public State applyEffect(State iState) {
		State toReturn = iState.copy();
		
		for (ObjectInstance o: toReturn.getObjectsOfTrueClass(this.objectClassEffected.name)) {
				o.setValue(this.atEffected.name, valueToSet);
		}
		
		return toReturn;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof AssignmentEffect && 
				((AssignmentEffect) o).getObjectClassEffected().equals(this.objectClassEffected) &&
				((AssignmentEffect) o).getAttributeAffected().equals(this.atEffected) &&
				((AssignmentEffect) o).getValueToSet() == this.valueToSet) {
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "Assigmenteffect of setting value to " + this.valueToSet + " for " + atEffected.name + " of " + objectClassEffected.name + "s";
	}

}
